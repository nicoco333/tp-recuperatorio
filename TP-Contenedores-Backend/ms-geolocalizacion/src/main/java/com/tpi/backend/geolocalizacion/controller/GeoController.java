package com.tpi.backend.geolocalizacion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tpi.backend.geolocalizacion.dto.distanciageo.DistanciaDTO;
import com.tpi.backend.geolocalizacion.service.GeoService;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService servicioGeo;

    @GetMapping("/calculo-distancia")
    public DistanciaDTO calcular(@RequestParam String inicio, @RequestParam String fin) {
        try {
            return servicioGeo.calcularDistancia(inicio, fin);
        } catch (Exception ex) {
            throw new RuntimeException("Fallo en cálculo de coordenadas: " + ex.getMessage(), ex);
        }
    }
}