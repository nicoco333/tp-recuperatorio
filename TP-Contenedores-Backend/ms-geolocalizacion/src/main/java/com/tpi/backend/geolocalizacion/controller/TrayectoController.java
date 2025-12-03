package com.tpi.backend.geolocalizacion.controller;

import com.tpi.backend.geolocalizacion.dto.*;
import com.tpi.backend.geolocalizacion.dto.distanciageo.DistanciaDTO;
import com.tpi.backend.geolocalizacion.service.RutaService;
import com.tpi.backend.geolocalizacion.util.RutaMapper;
import entities.Ruta;
import entities.Tramo;
import entities.Deposito;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/rutas")
public class TrayectoController {

    private final RutaService servicio;
    private final RutaMapper convertidor;

    public TrayectoController(RutaService servicio, RutaMapper convertidor) {
        this.servicio = servicio;
        this.convertidor = convertidor;
    }

    @GetMapping
    public ResponseEntity<List<RutaDTO>> obtenerTodas(@RequestParam(required = false) Integer id) {
        var lista = servicio.listarRutas(id).stream()
                .map(convertidor::toRutaDTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}/metricas")
    public ResponseEntity<DistanciaTotalRutaDTO> obtenerMetricas(@PathVariable Integer id) {
        return ResponseEntity.ok(servicio.obtenerDistanciaTotalRuta(id));
    }

    @PostMapping
    public ResponseEntity<?> nuevaRuta(@RequestBody RutaDTO info) {
        try {
            Ruta creada = servicio.crearRuta(convertidor.toRutaEntity(info));
            return ResponseEntity.status(HttpStatus.CREATED).body(convertidor.toRutaDTO(creada));
        } catch (IllegalArgumentException e) {
            return responderError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<?> verTramos(@PathVariable Integer id) {
        try {
            var tramos = servicio.listarTramosPorRuta(id).stream()
                    .map(convertidor::toTramoDTO)
                    .toList();
            return ResponseEntity.ok(tramos);
        } catch (EntityNotFoundException e) {
            return responderError(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/tramo/{id}/info-distancia")
    public ResponseEntity<DistanciaDTO> infoTramo(@PathVariable Integer id) {
        return ResponseEntity.ok(servicio.obtenerDistanciaDeTramo(id));
    }

    @PostMapping("/tramo")
    public ResponseEntity<TramoDTO> nuevoTramo(@RequestBody TramoDTO dato) {
        Tramo entidad = convertidor.toTramoEntity(dato);
        Tramo guardado = servicio.crearTramo(entidad);
        return ResponseEntity.ok(convertidor.toTramoDTO(guardado));
    }

    @PutMapping("/tramo/{id}/asignar-unidad")
    public ResponseEntity<TramoDTO> setUnidad(@PathVariable Integer id, @RequestBody AsignarCamionDTO unidad) {
        Tramo actualizado = servicio.asignarCamionATramo(id, unidad.getDominioCamion());
        return ResponseEntity.ok(convertidor.toTramoDTO(actualizado));
    }

    @PutMapping("/tramo/{id}/marcar-inicio")
    public ResponseEntity<TramoDTO> iniciar(@PathVariable Integer id, @RequestBody TiemposTramoDTO tiempo) {
        Tramo t = servicio.registrarInicioTramo(id, tiempo.getFechaHoraInicioReal());
        return ResponseEntity.ok(convertidor.toTramoDTO(t));
    }

    @PutMapping("/tramo/{id}/marcar-fin")
    public ResponseEntity<TramoDTO> finalizar(@PathVariable Integer id, @RequestBody TiemposTramoDTO tiempo) {
        Tramo t = servicio.registrarFinTramo(id, tiempo.getFechaHoraFinReal());
        return ResponseEntity.ok(convertidor.toTramoDTO(t));
    }

    @GetMapping("/puntos-deposito")
    public ResponseEntity<List<DepositoDTO>> verDepositos() {
        var lista = servicio.listarDepositos().stream()
                .map(convertidor::toDepositoDTO)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/puntos-deposito")
    public ResponseEntity<?> nuevoDeposito(@RequestBody DepositoDTO dato) {
        try {
            Deposito d = servicio.crearDeposito(convertidor.toDepositoEntity(dato));
            return ResponseEntity.status(HttpStatus.CREATED).body(convertidor.toDepositoDTO(d));
        } catch (IllegalArgumentException e) {
            return responderError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private ResponseEntity<ErrorResponseDTO> responderError(HttpStatus estado, String mensaje) {
        ErrorResponseDTO err = new ErrorResponseDTO(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), mensaje, "");
        return ResponseEntity.status(estado).body(err);
    }
}