package com.tpi.backend.mssolicitudes.client;

import com.tpi.backend.mssolicitudes.dto.DistanciaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

        // 1. Obtener el token actual del contexto de seguridad
        String token = "";
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            token = jwtAuth.getToken().getTokenValue();
        }

        // 2. Crear cabeceras con el token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // 3. Hacer la petición usando exchange (para pasar headers)
        ResponseEntity<DistanciaDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                DistanciaDTO.class
        );

        return response.getBody();
    }
}