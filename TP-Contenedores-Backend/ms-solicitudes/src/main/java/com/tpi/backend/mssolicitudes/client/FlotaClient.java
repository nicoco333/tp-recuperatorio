package com.tpi.backend.mssolicitudes.client;

import com.tpi.backend.mssolicitudes.dto.CamionFlotaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

    // Helper para obtener headers con token
    private HttpEntity<Void> getRequestEntity() {
        String token = "";
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            token = jwtAuth.getToken().getTokenValue();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    public Double calcularCosto(String tipoContenedor, double distanciaKm, double pesoKg) {
        String url = UriComponentsBuilder
                .fromHttpUrl(flotaBaseUrl + "/tarifas/calcular")
                .queryParam("tipoContenedor", tipoContenedor)
                .queryParam("distancia", distanciaKm)
                .queryParam("peso", pesoKg)
                .toUriString();

        ResponseEntity<Double> response = restTemplate.exchange(
                url, HttpMethod.GET, getRequestEntity(), Double.class
        );
        return response.getBody();
    }

    public CamionFlotaDTO obtenerCamionPorDominio(String dominio) {
        String url = flotaBaseUrl + "/camiones/" + dominio;
        
        ResponseEntity<CamionFlotaDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, getRequestEntity(), CamionFlotaDTO.class
        );
        return response.getBody();
    }
}