package com.tpi.backend.geolocalizacion.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient crearClienteTransporte(@Value("${ms.transporte.url}") String urlBase) {
        return RestClient.builder()
                .baseUrl(urlBase)
                .build();
    }
}