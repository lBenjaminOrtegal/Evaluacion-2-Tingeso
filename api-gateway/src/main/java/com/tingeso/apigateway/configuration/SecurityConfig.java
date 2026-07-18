package com.tingeso.apigateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${ALLOWED_ORIGINS:http://localhost:5173,http://localhost:8070,http://localhost:8080}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOrigins(allowedOrigins);
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange((auth) -> auth
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/api/tour-packages/sync/**").denyAll()
                        .pathMatchers("/api/search/tour-packages/sync/**").denyAll()
                        .pathMatchers("/internal/**").denyAll()
                        .pathMatchers(HttpMethod.GET, // m2
                                "/api/tour-packages",
                                "/api/tour-packages/{id}"
                        ).permitAll()
                        .pathMatchers(HttpMethod.GET, // m3
                                "/api/search/tour-packages",
                                "/api/search/tour-packages/filters",
                                "/api/search/tour-packages/{id}"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }
}
