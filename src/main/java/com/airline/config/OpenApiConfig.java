package com.airline.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI airlineReservationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Airline Reservation API")
                        .description("Versioned REST API for managing flights, reservations, passengers, and baggage.")
                        .version("v1")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project README")
                        .url("https://github.com/<your-user>/AIRLINE-RESERVATION-SYSTEM"));
    }
}

