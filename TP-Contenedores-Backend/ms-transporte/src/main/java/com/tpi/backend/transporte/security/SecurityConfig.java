package com.tpi.backend.transporte.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain reglasSeguridad(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Desactivar CSRF para APIs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/**").hasRole("OPERADOR")
                .requestMatchers(HttpMethod.PUT, "/**").hasRole("OPERADOR")
                .requestMatchers(HttpMethod.GET, "/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth -> oauth
                .jwt(token -> token.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
            );
        return http.build();
    }
}