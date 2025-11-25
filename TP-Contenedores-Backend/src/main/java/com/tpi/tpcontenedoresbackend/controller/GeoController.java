package com.tpi.tpcontenedoresbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tpi.tpcontenedoresbackend.model.DistanciaDTO; // <-- Importante
import com.tpi.tpcontenedoresbackend.service.GeoService; // <-- Importante

@RestController
@RequestMapping("/api/distancia")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;

    @GetMapping
    public DistanciaDTO obtenerDistancia(
            @RequestParam String origen,
            @RequestParam String destino) throws Exception {

        return geoService.calcularDistancia(origen, destino);
    }
}