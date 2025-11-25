package com.backend.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ruta para el microservicio de solicitudes
                .route("solicitudes", r -> r.path("/api/solicitudes/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://ms-solicitudes:8080"))

                // Ruta para el microservicio de rutas
                .route("rutas", r -> r.path("/api/rutas/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://ms-rutas:8080"))

                // Ruta para el microservicio de flota
                .route("flota", r -> r.path("/api/flota/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://ms-flota:8080"))
                .build();
    }
}