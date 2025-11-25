package com.tpi.backend.msflota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// 🔹 Le indicamos dónde buscar las entidades JPA (el módulo common-data)
@EntityScan(basePackages = {"entities"})
// 🔹 Le indicamos dónde están los repositorios JPA de este microservicio
@EnableJpaRepositories(basePackages = {"com.tpi.backend.msflota.repository"})
public class MsFlotaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsFlotaApplication.class, args);
    }

}
