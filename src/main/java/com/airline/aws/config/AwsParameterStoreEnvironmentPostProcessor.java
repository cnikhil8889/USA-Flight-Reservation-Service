package com.airline.aws.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Optional EnvironmentPostProcessor that can pull selected configuration values
 * from AWS Systems Manager Parameter Store when explicitly enabled.
 *
 * Activation:
 * - Set `aws.parameterstore.enabled=true` (for example via environment variable or profile config)
 * - Ensure an AWS region is configured via `aws.region` or `AWS_REGION`
 *
 * This is designed to be safe for local development: if AWS is not reachable
 * or parameters are missing, it logs a warning and falls back to existing properties.
 */
public class AwsParameterStoreEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(AwsParameterStoreEnvironmentPostProcessor.class);

    private static final String PROPERTY_SOURCE_NAME = "awsParameterStore";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean enabled = environment.getProperty("aws.parameterstore.enabled", Boolean.class, false);
        if (!enabled) {
            return;
        }

        String regionValue = environment.getProperty("aws.region",
                environment.getProperty("AWS_REGION"));
        if (regionValue == null || regionValue.isBlank()) {
            log.warn("aws.parameterstore.enabled=true but no aws.region/AWS_REGION configured; skipping Parameter Store integration");
            return;
        }

        Region region = Region.of(regionValue);
        log.info("Initializing AWS Parameter Store integration for region={}", region.id());

        try (SsmClient ssmClient = SsmClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            Map<String, Object> props = new HashMap<>();

            // Map of SSM parameter names to Spring property keys.
            Map<String, String> mappings = Map.of(
                    "/airline-reservation/spring.datasource.url", "spring.datasource.url",
                    "/airline-reservation/spring.datasource.username", "spring.datasource.username",
                    "/airline-reservation/spring.datasource.password", "spring.datasource.password",
                    "/airline-reservation/airline.baggage.pricing.checked-base-fee", "airline.baggage.pricing.checked-base-fee",
                    "/airline-reservation/airline.baggage.pricing.overweight-per-kg", "airline.baggage.pricing.overweight-per-kg"
            );

            mappings.forEach((parameterName, propertyKey) -> {
                try {
                    GetParameterResponse response = ssmClient.getParameter(
                            GetParameterRequest.builder()
                                    .name(parameterName)
                                    .withDecryption(true)
                                    .build()
                    );
                    String value = response.parameter().value();
                    props.put(propertyKey, value);
                    log.info("Loaded config from SSM: {} -> {}", parameterName, propertyKey);
                } catch (Exception e) {
                    // Missing parameters are not fatal; we fall back to existing configuration.
                    log.debug("Parameter {} not found or not accessible; keeping existing value for {}", parameterName, propertyKey);
                }
            });

            if (!props.isEmpty()) {
                MutablePropertySources propertySources = environment.getPropertySources();
                propertySources.addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, props));
                log.info("AWS Parameter Store property source '{}' registered with {} entries", PROPERTY_SOURCE_NAME, props.size());
            } else {
                log.info("No parameters loaded from AWS Parameter Store; existing configuration remains unchanged");
            }

        } catch (Exception e) {
            log.warn("Failed to initialize AWS Parameter Store integration; continuing with existing configuration. Cause: {}", e.getMessage());
        }
    }
}

