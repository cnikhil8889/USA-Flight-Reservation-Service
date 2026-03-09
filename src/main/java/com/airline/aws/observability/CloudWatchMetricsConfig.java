package com.airline.aws.observability;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Registers a Micrometer CloudWatch registry when an AWS-related profile is active.
 *
 * This reuses the existing application metrics and forwards them to Amazon CloudWatch
 * using the default AWS credentials provider chain.
 */
@Configuration
@Profile({"aws-dev", "aws-prod", "aws-observability"})
public class CloudWatchMetricsConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudWatchMetricsConfig.class);

    @Bean
    public CloudWatchConfig cloudWatchConfig(Environment environment) {
        String namespace = environment.getProperty("aws.cloudwatch.namespace", "AirlineReservation");
        String step = environment.getProperty("aws.cloudwatch.step", "60s");

        Map<String, String> settings = new HashMap<>();
        settings.put("cloudwatch.namespace", namespace);
        settings.put("cloudwatch.step", step);

        return new CloudWatchConfig() {
            @Override
            public String get(String key) {
                return settings.get(key);
            }
        };
    }

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient(Environment environment) {
        String region = environment.getProperty("aws.region",
                environment.getProperty("AWS_REGION", "us-east-1"));
        Region awsRegion = Region.of(region);
        log.info("Configuring CloudWatchAsyncClient with region={}", awsRegion.id());
        return CloudWatchAsyncClient.builder()
                .region(awsRegion)
                .build();
    }

    @Bean
    public CloudWatchMeterRegistry cloudWatchMeterRegistry(
            CloudWatchConfig cloudWatchConfig,
            CloudWatchAsyncClient cloudWatchAsyncClient
    ) {
        return CloudWatchMeterRegistry.builder(cloudWatchConfig, Clock.SYSTEM, cloudWatchAsyncClient)
                .build();
    }

    /**
     * Ensure a CompositeMeterRegistry is available so existing code that autowires
     * MeterRegistry continues to work when CloudWatch is enabled.
     */
    @Bean
    public MeterRegistry meterRegistry(CloudWatchMeterRegistry cloudWatchMeterRegistry) {
        CompositeMeterRegistry composite = new CompositeMeterRegistry(Clock.SYSTEM);
        composite.add(cloudWatchMeterRegistry);
        return composite;
    }
}

