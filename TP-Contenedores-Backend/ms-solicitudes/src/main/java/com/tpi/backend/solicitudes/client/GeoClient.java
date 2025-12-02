package com.tpi.backend.solicitudes.client;

import com.tpi.backend.solicitudes.dto.DistanciaDTO;
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
public class GeoClient {

    private final RestTemplate restTemplate;
    private final String urlBaseGeo;

    public GeoClient(RestTemplate restTemplate,
                       @Value("${ms.geolocalizacion.url}") String urlBaseGeo) {
        this.restTemplate = restTemplate;
        this.urlBaseGeo = urlBaseGeo;
    }

    private HttpEntity<Void> crearEntidadConAuth() {
        String tokenJwt = "";
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacion instanceof JwtAuthenticationToken jwt) {
            tokenJwt = jwt.getToken().getTokenValue();
        }

        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setBearerAuth(tokenJwt);
        return new HttpEntity<>(cabeceras);
    }

    public DistanciaDTO consultarDistancia(String puntoA, String puntoB) {
        String urlCompleta = UriComponentsBuilder
                .fromHttpUrl(urlBaseGeo + "/api/geo/calculo-distancia")
                .queryParam("inicio", puntoA)
                .queryParam("fin", puntoB)
                .toUriString();

        ResponseEntity<DistanciaDTO> respuesta = restTemplate.exchange(
                urlCompleta,
                HttpMethod.GET,
                crearEntidadConAuth(),
                DistanciaDTO.class
        );

        return respuesta.getBody();
    }
}