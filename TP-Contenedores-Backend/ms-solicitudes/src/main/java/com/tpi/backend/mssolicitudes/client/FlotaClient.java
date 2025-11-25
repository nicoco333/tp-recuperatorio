package com.tpi.backend.mssolicitudes.client;

import com.tpi.backend.mssolicitudes.dto.CamionFlotaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FlotaClient {

    private final RestTemplate restTemplate;
    private final String flotaBaseUrl;

    public FlotaClient(RestTemplate restTemplate,
                       @Value("${ms.flota.url}") String flotaBaseUrl) {
        this.restTemplate = restTemplate;
        this.flotaBaseUrl = flotaBaseUrl;
    }

    public Double calcularCosto(String tipoContenedor, double distanciaKm, double pesoKg) {
        String url = UriComponentsBuilder
                .fromHttpUrl(flotaBaseUrl + "/tarifas/calcular")
                .queryParam("tipoContenedor", tipoContenedor)
                .queryParam("distancia", distanciaKm)
                .queryParam("peso", pesoKg)
                .toUriString();

        return restTemplate.getForObject(url, Double.class);
    }

    public CamionFlotaDTO obtenerCamionPorDominio(String dominio) {
        // asumiendo que en ms-flota tengas algo como GET /camiones/{dominio}
        String url = flotaBaseUrl + "/camiones/" + dominio;
        return restTemplate.getForObject(url, CamionFlotaDTO.class);
    }
}
