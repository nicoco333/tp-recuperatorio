package com.tpi.backend.geolocalizacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EntityScan(basePackages = {"entities"})
@EnableJpaRepositories(basePackages = {"com.tpi.backend.geolocalizacion.repository"})
public class MsGeolocalizacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsGeolocalizacionApplication.class, args);
    }

    @Bean
    public RestTemplate plantillaRest() {
        return new RestTemplate();
    }

    @Bean
    public RestClient.Builder constructorClienteRest() {
        return RestClient.builder();
    }
}