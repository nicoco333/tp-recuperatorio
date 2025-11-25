package com.tpi.backend.msflota.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // OPERADOR puede gestionar (crear y actualizar) todos los recursos de esta API.
                        .requestMatchers(HttpMethod.POST, "/**").hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.PUT, "/**").hasRole("OPERADOR")

                        // TODOS los roles autenticados pueden consultar la información.
                        .requestMatchers(HttpMethod.GET, "/**").authenticated()

                        // Cualquier otra petición no definida debe estar autenticada.
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())
                        )
                );
        return http.build();
    }
}