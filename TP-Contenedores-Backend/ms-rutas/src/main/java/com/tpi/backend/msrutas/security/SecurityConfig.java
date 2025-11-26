package com.tpi.backend.msrutas.security;

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

                        // OPERADOR es el único que puede crear (POST) rutas, tramos y depósitos.
                        .requestMatchers(HttpMethod.POST, "/**").hasRole("OPERADOR")

                        .requestMatchers(HttpMethod.PUT, "/tramos/*/camion")
                        .hasAnyRole("OPERADOR", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/tramos/*/inicio")
                        .hasAnyRole("OPERADOR", "TRANSPORTISTA")

                        .requestMatchers(HttpMethod.PUT, "/tramos/*/fin")
                        .hasAnyRole("OPERADOR", "TRANSPORTISTA")

                        // Permisos de LECTURA (GET)
                        // Se agrega "CLIENTE" para que pueda consultar distancias (vía ms-solicitudes) y ver sus rutas.
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("OPERADOR", "TRANSPORTISTA", "CLIENTE")

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