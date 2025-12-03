package com.tpi.backend.geolocalizacion.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class SolicitudClient {

    private final RestClient restClient;

    public SolicitudClient(RestClient.Builder builder, 
                           @Value("${ms.solicitudes.url}") String urlSolicitudes) {
        this.restClient = builder.baseUrl(urlSolicitudes).build();
    }

    public void notificarProgreso(Integer idSolicitud, 
                                  Double cEst, Integer tEst, 
                                  Double cReal, Integer tReal, 
                                  String estado) {
        
        String token = obtenerToken();
        Map<String, Object> body = new HashMap<>();
        
        // Solo agregamos lo que no sea null
        if (cEst != null) body.put("costoEstimado", cEst);
        if (tEst != null) body.put("tiempoEstimado", tEst);
        if (cReal != null) body.put("costoReal", cReal);
        if (tReal != null) body.put("tiempoReal", tReal);
        if (estado != null) body.put("estado", estado);

        if (body.isEmpty()) return;

        try {
            restClient.patch()
                    .uri("/" + idSolicitud + "/progreso")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> h.setBearerAuth(token))
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Error notificando progreso a ms-solicitudes: " + e.getMessage());
        }
    }

    private String obtenerToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return jwt.getToken().getTokenValue();
        }
        return "";
    }
}