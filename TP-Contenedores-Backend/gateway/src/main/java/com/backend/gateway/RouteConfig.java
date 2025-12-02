package com.backend.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator configurarEnrutamiento(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("route_pedidos", p -> p.path("/api/solicitudes/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://ms-solicitudes:8080"))
                .route("route_geo", p -> p.path("/api/geolocalizacion/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://ms-geolocalizacion:8080"))
                .route("route_transporte", p -> p.path("/api/transporte/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("http://ms-transporte:8080"))
                .build();
    }
}