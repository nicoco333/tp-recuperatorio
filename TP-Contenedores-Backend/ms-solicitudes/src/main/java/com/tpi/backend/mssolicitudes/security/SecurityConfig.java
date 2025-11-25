package com.tpi.backend.mssolicitudes.security;

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
                        // Reglas para el ROL CLIENTE
                        .requestMatchers(HttpMethod.POST, "/").hasRole("CLIENTE") // Crear solicitud
                        .requestMatchers(HttpMethod.PUT, "/{nroSolicitud}").hasRole("CLIENTE") // Actualizar su solicitud
                        .requestMatchers(HttpMethod.POST, "/solicitudes/{nroSolicitud}/tarifa").hasRole("CLIENTE") // Calcular su tarifa
                        .requestMatchers(HttpMethod.GET, "/contenedores/{idContenedor}/estado").hasRole("CLIENTE") // Ver estado de su contenedor

                        //Reglas para el ROL OPERADOR
                        .requestMatchers(HttpMethod.POST, "/clientes", "/contenedores", "/estados").hasRole("OPERADOR") // Crear datos maestros

                        //Reglas de LECTURA
                        // El CLIENTE y el OPERADOR pueden consultar la información.
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("CLIENTE", "OPERADOR")

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