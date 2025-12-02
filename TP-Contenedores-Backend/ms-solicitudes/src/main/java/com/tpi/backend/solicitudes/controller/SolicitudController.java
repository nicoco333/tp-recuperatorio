package com.tpi.backend.solicitudes.controller;

import com.tpi.backend.mssolicitudes.dto.*;
import com.tpi.backend.mssolicitudes.service.SolicitudService;
import com.tpi.backend.mssolicitudes.util.SolicitudMapper;
import entities.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/")
public class SolicitudController {

    private final SolicitudService servicio;
    private final SolicitudMapper convertidor;

    public SolicitudController(SolicitudService servicio, SolicitudMapper convertidor) {
        this.servicio = servicio;
        this.convertidor = convertidor;
    }

    @GetMapping
    public List<SolicitudDTO> obtenerTodas(@RequestParam(required = false) Integer id) {
        return servicio.buscarSolicitudes(id).stream()
                .map(convertidor::toSolicitudDTO)
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody SolicitudDTO dato, HttpServletRequest req) {
        try {
            Solicitud s = servicio.registrarSolicitud(convertidor.toSolicitudEntity(dato));
            return ResponseEntity.status(HttpStatus.CREATED).body(convertidor.toSolicitudDTO(s));
        } catch (IllegalArgumentException e) {
            return armarRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage(), req);
        } catch (EntityNotFoundException e) {
            return armarRespuestaError(HttpStatus.NOT_FOUND, e.getMessage(), req);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificar(@PathVariable Integer id, @RequestBody SolicitudDTO dato, HttpServletRequest req) {
        try {
            Solicitud s = servicio.modificarSolicitud(id, dato);
            return ResponseEntity.ok(convertidor.toSolicitudDTO(s));
        } catch (EntityNotFoundException e) {
            return armarRespuestaError(HttpStatus.NOT_FOUND, e.getMessage(), req);
        }
    }

    @GetMapping("/clientes")
    public List<ClienteDTO> verClientes(@RequestParam(required = false) Integer dni) {
        return servicio.obtenerClientes(dni).stream()
                .map(convertidor::toClienteDTO)
                .toList();
    }

    @PostMapping("/clientes")
    public ResponseEntity<?> nuevoCliente(@RequestBody ClienteDTO dato, HttpServletRequest req) {
        try {
            Cliente c = servicio.altaCliente(convertidor.toClienteEntity(dato));
            return ResponseEntity.status(HttpStatus.CREATED).body(convertidor.toClienteDTO(c));
        } catch (IllegalArgumentException e) {
            return armarRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage(), req);
        }
    }

    @GetMapping("/contenedores")
    public List<ContenedorDTO> buscarContenedores(@RequestParam(required = false) String estado,
                                                  @RequestParam(required = false) Integer dni) {
        return servicio.filtrarContenedores(estado, dni).stream()
                .map(convertidor::toContenedorDTO)
                .toList();
    }

    @GetMapping("/contenedores/{id}/estado")
    public ResponseEntity<?> verEstadoContenedor(@PathVariable Integer id, HttpServletRequest req) {
        try {
            return ResponseEntity.ok(servicio.consultarEstadoContenedor(id));
        } catch (Exception e) {
            return armarRespuestaError(HttpStatus.NOT_FOUND, e.getMessage(), req);
        }
    }

    @PostMapping("/contenedores")
    public ResponseEntity<?> nuevoContenedor(@RequestBody ContenedorDTO dato, HttpServletRequest req) {
        try {
            Contenedor c = servicio.altaContenedor(convertidor.toContenedorEntity(dato));
            return ResponseEntity.status(HttpStatus.CREATED).body(convertidor.toContenedorDTO(c));
        } catch (IllegalArgumentException e) {
            return armarRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage(), req);
        }
    }

    @GetMapping("/estados")
    public List<EstadoDTO> verEstados() {
        return servicio.obtenerEstados().stream()
                .map(convertidor::toEstadoDTO)
                .toList();
    }

    @PostMapping("/estados")
    public ResponseEntity<?> nuevoEstado(@RequestBody EstadoDTO dato) {
        Estado e = servicio.altaEstado(convertidor.toEstadoEntity(dato));
        return ResponseEntity.status(HttpStatus.CREATED).body(convertidor.toEstadoDTO(e));
    }

    @PostMapping("/solicitudes/{id}/tarifa")
    public ResponseEntity<?> cotizar(@PathVariable Integer id, HttpServletRequest req) {
        try {
            return ResponseEntity.ok(servicio.cotizarSolicitud(id));
        } catch (EntityNotFoundException | IllegalArgumentException | IllegalStateException e) {
            return armarRespuestaError(HttpStatus.BAD_REQUEST, e.getMessage(), req);
        } catch (Exception e) {
            return armarRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno: " + e.getMessage(), req);
        }
    }

    private ResponseEntity<ErrorResponseDTO> armarRespuestaError(HttpStatus estado, String mensaje, HttpServletRequest req) {
        ErrorResponseDTO err = new ErrorResponseDTO(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), mensaje, req.getRequestURI());
        return ResponseEntity.status(estado).body(err);
    }
}