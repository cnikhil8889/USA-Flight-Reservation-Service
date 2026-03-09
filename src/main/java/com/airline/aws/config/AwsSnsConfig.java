package com.airline.aws.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
@Profile({"aws-events", "aws-prod"})
public class AwsSnsConfig {

    private static final Logger log = LoggerFactory.getLogger(AwsSnsConfig.class);

    @Bean
    public SnsClient snsClient(Environment environment) {
        String region = environment.getProperty("aws.region",
                environment.getProperty("AWS_REGION", "us-east-1"));
        Region awsRegion = Region.of(region);
        log.info("Configuring SnsClient with region={}", awsRegion.id());
        return SnsClient.builder()
                .region(awsRegion)
                .build();
    }
}

