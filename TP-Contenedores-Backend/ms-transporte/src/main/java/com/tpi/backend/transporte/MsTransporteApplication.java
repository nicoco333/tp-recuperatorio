package com.tpi.backend.transporte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"entities"})
@EnableJpaRepositories(basePackages = {"com.tpi.backend.transporte.repository"})
public class MsTransporteApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsTransporteApplication.class, args);
    }
}