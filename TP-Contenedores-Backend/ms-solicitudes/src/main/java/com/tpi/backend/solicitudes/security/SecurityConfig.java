package com.tpi.backend.solicitudes.security;

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
    public SecurityFilterChain configuracionSeguridad(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Desactivar CSRF para APIs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.PUT, "/{id}").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.POST, "/solicitudes/*/tarifa").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/contenedores/*/estado").hasRole("CLIENTE")
                
                .requestMatchers(HttpMethod.POST, "/clientes", "/contenedores", "/estados").hasRole("OPERADOR")
                
                .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("CLIENTE", "OPERADOR")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth -> oauth
                .jwt(token -> token.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
            );
        return http.build();
    }
}