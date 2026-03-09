package com.airline.aws.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Default, development-friendly security configuration that leaves
 * the existing behavior (no authentication) unchanged when the
 * `aws-auth` profile is not active.
 */
@Configuration
@Profile("!aws-auth")
public class NoAuthSecurityConfig {

    @Bean
    public SecurityFilterChain noAuthSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}

