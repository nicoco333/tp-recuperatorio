package com.tpi.backend.msrutas.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tpi.backend.msrutas.dto.geolocalizacion.DistanciaDTO;
import com.tpi.backend.msrutas.service.GeoService;

@RestController
@RequestMapping("/distancia")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;

    @GetMapping
    public DistanciaDTO obtenerDistancia(
            @RequestParam String origen,
            @RequestParam String destino) {

        try {
            return geoService.calcularDistancia(origen, destino);
        } catch (Exception e) {
            System.err.println("ERROR: Ocurrió un error inesperado al calcular la distancia.");
            e.printStackTrace();
            throw new RuntimeException("Error en el cálculo de distancia, causa: " + e.getMessage(), e);
        }
    }
}