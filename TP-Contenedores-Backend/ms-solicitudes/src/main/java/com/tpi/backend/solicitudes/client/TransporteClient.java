package com.tpi.backend.solicitudes.client;

import com.tpi.backend.solicitudes.dto.CamionFlotaDTO;
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
public class TransporteClient {

    private final RestTemplate template;
    private final String urlTransporte;

    public TransporteClient(RestTemplate template,
                       @Value("${ms.transporte.url}") String urlTransporte) {
        this.template = template;
        this.urlTransporte = urlTransporte;
    }

    private HttpEntity<Void> generarHeaders() {
        String token = "";
        var auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth instanceof JwtAuthenticationToken jwtToken) {
            token = jwtToken.getToken().getTokenValue();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    public Double obtenerCostoBase(String tipo, double km, double carga) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(urlTransporte + "/tarifas/calcular")
                .queryParam("tipoContenedor", tipo)
                .queryParam("distancia", km)
                .queryParam("peso", carga)
                .toUriString();

        ResponseEntity<Double> res = template.exchange(
                uri, 
                HttpMethod.GET, 
                generarHeaders(), 
                Double.class
        );
        return res.getBody();
    }

    public CamionFlotaDTO buscarUnidad(String patente) {
        String uri = urlTransporte + "/camiones/" + patente;
        
        ResponseEntity<CamionFlotaDTO> res = template.exchange(
                uri, 
                HttpMethod.GET, 
                generarHeaders(), 
                CamionFlotaDTO.class
        );
        return res.getBody();
    }
}