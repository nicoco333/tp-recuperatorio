package com.tpi.backend.geolocalizacion.client;

import com.tpi.backend.geolocalizacion.dto.TarifaDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;

@Component
public class TarifaClient {

    private final RestClient client;

    public TarifaClient(RestClient client) {
        this.client = client;
    }

    public TarifaDTO obtenerTarifaPorCamion(String patente) {
        String jwt = obtenerTokenActual();

        TarifaDTO[] respuesta = client.get()
                .uri(uri -> uri.path("/tarifas")
                        .queryParam("dominio_camion", patente) 
                        .build())
                .headers(h -> {
                    if (jwt != null) h.setBearerAuth(jwt);
                })
                .retrieve()
                .body(TarifaDTO[].class);

        return (respuesta != null && respuesta.length > 0) ? respuesta[0] : null;
    }

    private String obtenerTokenActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
            return token.getToken().getTokenValue();
        }
        throw new RuntimeException("Error: Contexto de seguridad no contiene token JWT válido");
    }
}