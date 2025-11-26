package com.tpi.backend.msrutas.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpi.backend.msrutas.dto.geolocalizacion.DistanciaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class GeoService {

    @Value("${google.maps.apikey}")
    private String apiKey;

    private final RestClient.Builder builder;

    public DistanciaDTO calcularDistancia (String origen, String destino) throws Exception {
        // Usamos la URL base de Google Maps
        RestClient client = builder.baseUrl("https://maps.googleapis.com/maps/api").build();

        String url = "/distancematrix/json?origins=" + origen +
                "&destinations=" + destino +
                "&units=metric&key=" + apiKey;

        ResponseEntity<String> response;
        try {
            response = client.get().uri(url).retrieve().toEntity(String.class);
        } catch (RestClientException e) {
            System.err.println("ERROR HTTP: Fallo de comunicación con la API de Google Maps.");
            throw new Exception("Error de comunicación con Google Maps", e);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            // Validamos que Google haya respondido OK a nivel general
            if (!"OK".equals(root.path("status").asText())) {
                throw new Exception("Error de Google Maps: " + root.path("error_message").asText());
            }

            // Validamos que haya encontrado una ruta (evitar el NOT_FOUND de la vez pasada)
            JsonNode element = root.path("rows").get(0).path("elements").get(0);
            if (!"OK".equals(element.path("status").asText())) {
                 // Si Google no encuentra la calle, lanzamos error o devolvemos 0
                 throw new Exception("Google Maps no encontró ruta entre: " + origen + " y " + destino);
            }

            DistanciaDTO dto = new DistanciaDTO();
            dto.setOrigen(origen);
            dto.setDestino(destino);

            // Extraemos los valores reales
            dto.setKilometros(element.path("distance").path("value").asDouble() / 1000);
            dto.setDuracionTexto(element.path("duration").path("text").asText());

            long duracionSegundos = element.path("duration").path("value").asLong();
            dto.setDuracionMinutos(duracionSegundos / 60);

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}