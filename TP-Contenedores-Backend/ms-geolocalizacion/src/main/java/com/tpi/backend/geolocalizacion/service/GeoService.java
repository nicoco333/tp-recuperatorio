package com.tpi.backend.geolocalizacion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpi.backend.geolocalizacion.dto.distanciageo.DistanciaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class GeoService {

    @Value("${osrm.api.url}")
    private String osrmApiUrl;

    private final RestClient.Builder builder;

    /**
     * Calcula distancia usando OSRM. Las coordenadas deben venir en formato "lon,lat".
     */
    public DistanciaDTO calcularDistancia(String coordenadasOrigen, String coordenadasDestino) throws Exception {
        // Configuramos el cliente con la URL base de OSRM
        RestClient client = builder.baseUrl(osrmApiUrl).build();

        // OSRM usa el formato: /route/v1/driving/lon1,lat1;lon2,lat2?overview=false
        String url = "/route/v1/driving/" + coordenadasOrigen + ";" + coordenadasDestino + "?overview=false";

        ResponseEntity<String> response;
        try {
            response = client.get().uri(url).retrieve().toEntity(String.class);
        } catch (RestClientException e) {
            System.err.println("ERROR HTTP: Fallo de comunicación con el servidor OSRM.");
            throw new Exception("Error de comunicación con OSRM", e);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            // Validar código de respuesta de OSRM
            if (!"Ok".equalsIgnoreCase(root.path("code").asText())) {
                throw new Exception("Error de OSRM: " + root.path("message").asText());
            }

            // Extraer la ruta (routes array)
            JsonNode routes = root.path("routes");
            if (routes.isEmpty()) {
                throw new Exception("OSRM no encontró una ruta válida entre las coordenadas indicadas.");
            }

            JsonNode route = routes.get(0);

            DistanciaDTO dto = new DistanciaDTO();
            // OSRM no devuelve las direcciones en texto como Google, así que ponemos las coordenadas
            dto.setOrigen(coordenadasOrigen);
            dto.setDestino(coordenadasDestino);

            // OSRM devuelve distancia en METROS y duración en SEGUNDOS
            double metros = route.path("distance").asDouble();
            double segundos = route.path("duration").asDouble();

            // Convertimos a Km y Minutos para mantener consistencia con tu DTO
            dto.setKilometros(metros / 1000.0);
            dto.setDuracionMinutos((long) (segundos / 60));

            // Texto descriptivo simple
            long horas = dto.getDuracionMinutos() / 60;
            long mins = dto.getDuracionMinutos() % 60;
            dto.setDuracionTexto(horas > 0 ? String.format("%d h %d min", horas, mins) : String.format("%d min", mins));

            return dto;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}