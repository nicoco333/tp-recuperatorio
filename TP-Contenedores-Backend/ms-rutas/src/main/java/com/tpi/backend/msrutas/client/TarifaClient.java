package com.tpi.backend.msrutas.client;

import com.tpi.backend.msrutas.dto.TarifaDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

@Component
public class TarifaClient {

    private final RestClient flotaRestClient;

    public TarifaClient(RestClient flotaRestClient) {
        this.flotaRestClient = flotaRestClient;
    }

    public TarifaDTO obtenerTarifaPorCamion(String dominioCamion) {
        String token = currentTokenValue();

        TarifaDTO[] tarifas = flotaRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tarifas")
                        .queryParam("dominioCamion", dominioCamion)
                        .build())
                .headers(headers -> {
                    if (token != null) {
                        headers.setBearerAuth(token);   // <<< acá va el JWT
                    }
                })
                .retrieve()
                .body(TarifaDTO[].class);

        if (tarifas == null || tarifas.length == 0) {
            return null;
        }
        return tarifas[0];
    }

    private String currentTokenValue() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getTokenValue();
        }
        throw new IllegalStateException("No hay JwtAuthenticationToken en el SecurityContext");
    }

}
