package com.tpi.backend.mssolicitudes.client;

import com.tpi.backend.mssolicitudes.dto.DistanciaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class RutasClient {

    private final RestTemplate restTemplate;
    private final String rutasBaseUrl;

    public RutasClient(RestTemplate restTemplate,
                       @Value("${ms.rutas.url}") String rutasBaseUrl) {
        this.restTemplate = restTemplate;
        this.rutasBaseUrl = rutasBaseUrl;
    }

    public DistanciaDTO obtenerDistancia(String origen, String destino) {
        String url = UriComponentsBuilder
                .fromHttpUrl(rutasBaseUrl + "/distancia")
                .queryParam("origen", origen)
                .queryParam("destino", destino)
                .toUriString();

        return restTemplate.getForObject(url, DistanciaDTO.class);
    }
}
