package com.tpi.backend.geolocalizacion.security;

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
    public SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/**").hasRole("OPERADOR")
                .requestMatchers(HttpMethod.PUT, "/rutas/tramo/*/asignar-unidad").hasAnyRole("OPERADOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/rutas/tramo/*/marcar-inicio").hasAnyRole("OPERADOR", "TRANSPORTISTA")
                .requestMatchers(HttpMethod.PUT, "/rutas/tramo/*/marcar-fin").hasAnyRole("OPERADOR", "TRANSPORTISTA")
                .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("OPERADOR", "TRANSPORTISTA", "CLIENTE")
                .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
        );
        return http.build();
    }
}