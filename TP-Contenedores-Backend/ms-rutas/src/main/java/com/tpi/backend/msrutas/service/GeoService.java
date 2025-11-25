package com.tpi.backend.msrutas.service;

import com.fasterxml.jackson.core.JsonProcessingException; // <-- Nuevo Importe
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
        RestClient client = builder.baseUrl("https://maps.googleapis.com/maps/api").build();
        ResponseEntity<String> response;
        String url = "/distancematrix/json?origins=" + origen +
                "&destinations=" + destino +
                "&units=metric&key=" + apiKey;

        try {
            // 1. Llama a la API externa. Si falla la conexión, salta al primer catch.
            response = client.get().uri(url).retrieve().toEntity(String.class);
        } catch (RestClientException e) {
            // DIAGNÓSTICO HTTP/CONEXIÓN
            System.err.println("ERROR HTTP: Fallo de comunicación con la API de Google Maps.");
            e.printStackTrace();
            throw new Exception("Error de comunicación con Google Maps (revisar clave o permisos)", e);
        }

        // Si la llamada fue exitosa (código 200 OK):
        try {
            // 2. Intenta parsear la respuesta JSON. Esto puede fallar si el cuerpo es nulo o no es JSON.
            ObjectMapper mapper = new ObjectMapper();

            // Loguear el cuerpo antes de parsear para el diagnóstico
            System.out.println("Body de respuesta de Google Maps: " + response.getBody());

            JsonNode root = mapper.readTree(response.getBody());

            // 3. Verificación de errores de negocio de Google Maps (status != OK)
            if (!"OK".equals(root.path("status").asText())) {
                String status = root.path("status").asText();
                String errorMessage = root.path("error_message").asText();
                throw new Exception("Error de Google Maps (Status: " + status + "): " + errorMessage);
            }

            // Verificación de estructura mínima (para evitar NullPointerException)
            if (!root.path("rows").get(0).path("elements").get(0).has("distance")) {
                throw new Exception("Respuesta de Google Maps válida, pero sin datos de distancia.");
            }

            JsonNode leg = root.path("rows").get(0).path("elements").get(0);

            DistanciaDTO dto = new DistanciaDTO();
            dto.setOrigen(origen);
            dto.setDestino(destino);
            dto.setKilometros(leg.path("distance").path("value").asDouble() / 1000);
            dto.setDuracionTexto(leg.path("duration").path("text").asText());
            long duracionSegundos = leg.path("duration").path("value").asLong();
            long duracionMinutos = duracionSegundos / 60;
            dto.setDuracionMinutos(duracionMinutos);

            return dto;
        } catch (JsonProcessingException e) {
            // DIAGNÓSTICO PARSEO JSON
            System.err.println("ERROR JSON: Fallo al parsear la respuesta JSON de Google Maps.");
            System.err.println("Cuerpo Recibido: " + response.getBody());
            e.printStackTrace();
            throw new Exception("Error de procesamiento de la respuesta JSON", e);
        } catch (Exception e) {
            System.err.println("ERROR LÓGICA: Fallo al procesar el JSON válido.");
            e.printStackTrace();
            throw e;
        }
    }
}