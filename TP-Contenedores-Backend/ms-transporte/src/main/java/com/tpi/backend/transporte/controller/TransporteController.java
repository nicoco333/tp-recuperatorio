package com.tpi.backend.transporte.controller;

import com.tpi.backend.transporte.dto.*;
import com.tpi.backend.transporte.service.TransporteService;
import com.tpi.backend.transporte.util.TransporteMapper;
import entities.Camion;
import entities.Tarifa;
import entities.Transportista;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/")
public class TransporteController {

    private final TransporteService servicio;
    private final TransporteMapper convertidor;

    public TransporteController(TransporteService servicio, TransporteMapper convertidor) {
        this.servicio = servicio;
        this.convertidor = convertidor;
    }

    @GetMapping("/camiones")
    public ResponseEntity<?> verUnidades(@RequestParam(required = false) String dominio,
                                         @RequestParam(required = false) Boolean disponible,
                                         HttpServletRequest req) {
        try {
            var lista = servicio.buscarUnidades(dominio, disponible).stream()
                    .map(convertidor::toCamionDTO)
                    .toList();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return responderError(HttpStatus.INTERNAL_SERVER_ERROR, "Fallo al listar: " + e.getMessage(), req);
        }
    }

    @GetMapping("/camiones/disponibles")
    public List<CamionDTO> soloDisponibles() {
        return servicio.listarDisponibles().stream()
                .map(convertidor::toCamionDTO)
                .toList();
    }

    @PostMapping("/camiones")
    public ResponseEntity<?> altaUnidad(@RequestBody CamionDTO dato, HttpServletRequest req) {
        try {
            Camion c = servicio.altaCamion(convertidor.toCamionEntity(dato));
            return ResponseEntity.status(HttpStatus.CREATED).body(convertidor.toCamionDTO(c));
        } catch (IllegalArgumentException e) {
            return responderError(HttpStatus.BAD_REQUEST, e.getMessage(), req);
        } catch (EntityNotFoundException e) {
            return responderError(HttpStatus.NOT_FOUND, e.getMessage(), req);
        }
    }

    @PutMapping("/camiones/{patente}")
    public ResponseEntity<?> modificarUnidad(@PathVariable String patente, @RequestBody CamionDTO dato, HttpServletRequest req) {
        try {
            Camion actualizado = servicio.modificarCamion(patente, dato);
            return ResponseEntity.ok(convertidor.toCamionDTO(actualizado));
        } catch (EntityNotFoundException e) {
            return responderError(HttpStatus.NOT_FOUND, e.getMessage(), req);
        } catch (IllegalArgumentException e) {
            return responderError(HttpStatus.BAD_REQUEST, e.getMessage(), req);
        }
    }

    @GetMapping("/transportistas")
    public List<TransportistaDTO> verTransportistas() {
        return servicio.obtenerTransportistas().stream()
                .map(convertidor::toTransportistaDTO)
                .toList();
    }

    @PostMapping("/transportistas")
    public TransportistaDTO nuevoTransportista(@RequestBody TransportistaDTO dato) {
        Transportista t = servicio.altaTransportista(convertidor.toTransportistaEntity(dato));
        return convertidor.toTransportistaDTO(t);
    }

    @GetMapping("/tarifas")
    public List<TarifaDTO> verTarifas(@RequestParam(required = false) String patente) {
        return servicio.consultarTarifas(patente).stream()
                .map(convertidor::toTarifaDTO)
                .toList();
    }

    @PostMapping("/tarifas")
    public TarifaDTO nuevaTarifa(@RequestBody TarifaDTO dato) {
        Tarifa t = servicio.nuevaTarifa(convertidor.toTarifaEntity(dato), dato.getDominioCamion());
        return convertidor.toTarifaDTO(t);
    }

    @GetMapping("/tarifas/calcular")
    public ResponseEntity<Double> calcular(@RequestParam String tipoContenedor,
                                           @RequestParam Double distancia,
                                           @RequestParam Double peso) {
        return ResponseEntity.ok(servicio.estimarCosto(tipoContenedor, distancia, peso));
    }

    private ResponseEntity<ErrorResponseDTO> responderError(HttpStatus estado, String mensaje, HttpServletRequest req) {
        ErrorResponseDTO err = new ErrorResponseDTO(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), mensaje, req.getRequestURI());
        return ResponseEntity.status(estado).body(err);
    }
}