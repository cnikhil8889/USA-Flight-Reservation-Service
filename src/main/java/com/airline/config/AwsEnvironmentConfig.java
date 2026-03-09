package com.airline.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Lightweight configuration wrapper to make it easy to map
 * AWS-style environment variables (or SSM/Secrets Manager values
 * exposed as env vars) onto Spring properties.
 *
 * This is intentionally minimal and non-invasive: the application
 * still works with local properties, but can be configured via
 * environment on AWS.
 */
@Configuration
public class AwsEnvironmentConfig {

    private static final Logger log = LoggerFactory.getLogger(AwsEnvironmentConfig.class);

    public AwsEnvironmentConfig(
            @Value("${DB_URL:${spring.datasource.url:}}") String dbUrl,
            @Value("${DB_USERNAME:${spring.datasource.username:}}") String dbUser,
            @Value("${DB_PASSWORD:${spring.datasource.password:}}") String dbPassword,
            @Value("${AWS_REGION:}") String awsRegion
    ) {
        // These logs are safe; they avoid printing secrets, but confirm wiring.
        log.info("Database URL resolved for startup: {}", maskUrl(dbUrl));
        log.info("Database username resolved for startup: {}", dbUser != null ? "***" : "not set");
        if (!awsRegion.isEmpty()) {
            log.info("Running with AWS_REGION={}", awsRegion);
        }
    }

    private String maskUrl(String url) {
        if (url == null || url.isBlank()) {
            return "not set";
        }
        // Avoid leaking credentials in JDBC URLs
        return url.replaceAll("(?i)(password=)[^&]+", "$1****");
    }
}

