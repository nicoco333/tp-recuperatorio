package com.tpi.backend.solicitudes.controller;

import com.tpi.backend.solicitudes.dto.*;
import com.tpi.backend.solicitudes.service.SolicitudService;
import com.tpi.backend.solicitudes.util.SolicitudMapper;
import entities.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    public List<SolicitudDTO> obtenerTodas(@RequestParam(required = false) Integer id,
                                           @RequestParam(required = false) Integer dniCliente) {
        return servicio.buscarSolicitudes(id, dniCliente).stream()
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

    // --- ENDPOINT CORREGIDO PARA RECIBIR TODOS LOS DATOS ---
    @PatchMapping("/{id}/progreso")
    public ResponseEntity<?> actualizarProgreso(@PathVariable Integer id, @RequestBody Map<String, Object> body, HttpServletRequest req) {
        try {
            // Extraer con seguridad (pueden venir nulos)
            Double cEst = body.get("costoEstimado") != null ? Double.valueOf(body.get("costoEstimado").toString()) : null;
            Integer tEst = body.get("tiempoEstimado") != null ? Integer.valueOf(body.get("tiempoEstimado").toString()) : null;
            
            Double cReal = body.get("costoReal") != null ? Double.valueOf(body.get("costoReal").toString()) : null;
            Integer tReal = body.get("tiempoReal") != null ? Integer.valueOf(body.get("tiempoReal").toString()) : null;
            
            String estado = (String) body.get("estado");
            
            servicio.actualizarProgreso(id, cEst, tEst, cReal, tReal, estado);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return armarRespuestaError(HttpStatus.BAD_REQUEST, "Error actualizando progreso: " + e.getMessage(), req);
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


    private ResponseEntity<ErrorResponseDTO> armarRespuestaError(HttpStatus estado, String mensaje, HttpServletRequest req) {
        ErrorResponseDTO err = new ErrorResponseDTO(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), mensaje, req.getRequestURI());
        return ResponseEntity.status(estado).body(err);
    }
}