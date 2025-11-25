package com.tpi.backend.mssolicitudes.controller;

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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final SolicitudMapper mapper;

    public SolicitudController(SolicitudService solicitudService, SolicitudMapper mapper) {
        this.solicitudService = solicitudService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<SolicitudDTO> listarSolicitudes(@RequestParam(required = false) Integer nroSolicitud) {
        return solicitudService.listarSolicitudes(nroSolicitud)
                .stream()
                .map(mapper::toSolicitudDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> crearSolicitud(@RequestBody SolicitudDTO dto, HttpServletRequest request) {
        try {
            Solicitud solicitud = solicitudService.crearSolicitud(mapper.toSolicitudEntity(dto));
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toSolicitudDTO(solicitud));
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (EntityNotFoundException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Not Found", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{nroSolicitud}")
    public ResponseEntity<?> actualizarSolicitud(@PathVariable Integer nroSolicitud, @RequestBody SolicitudDTO dto, HttpServletRequest request) {
        try {
            Solicitud solicitudActualizada = solicitudService.actualizarSolicitud(nroSolicitud, dto);
            return ResponseEntity.ok(mapper.toSolicitudDTO(solicitudActualizada));
        } catch (EntityNotFoundException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Not Found", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/clientes")
    public List<ClienteDTO> listarClientes(@RequestParam(required = false) Integer dni) {
        return solicitudService.listarClientes(dni)
                .stream()
                .map(mapper::toClienteDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/clientes")
    public ResponseEntity<?> crearCliente(@RequestBody ClienteDTO dto, HttpServletRequest request) {
        try {
            Cliente cliente = solicitudService.crearCliente(mapper.toClienteEntity(dto));
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toClienteDTO(cliente));
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/contenedores")
    public List<ContenedorDTO> listarContenedores(@RequestParam(required = false) String estado,
                                                  @RequestParam(required = false) Integer dniCliente) {
        List<Contenedor> contenedores;
        if (dniCliente != null && estado != null && !estado.isBlank()) {
            contenedores = solicitudService.listarContenedoresPorDniYEstado(dniCliente, estado);
        } else if (dniCliente != null) {
            contenedores = solicitudService.listarContenedoresPorDniCliente(dniCliente);
        } else if (estado != null && !estado.isBlank()) {
            contenedores = solicitudService.listarContenedoresPorEstadoNombre(estado);
        } else {
            contenedores = solicitudService.listarContenedores();
        }
        return contenedores.stream().map(mapper::toContenedorDTO).collect(Collectors.toList());
    }

    @GetMapping("/contenedores/{idContenedor}/estado")
    public ResponseEntity<?> obtenerEstadoContenedor(@PathVariable Integer idContenedor, HttpServletRequest request) {
        try {
            EstadoDTO estadoDTO = solicitudService.obtenerEstadoActualDeContenedor(idContenedor);
            return ResponseEntity.ok(estadoDTO);
        } catch (EntityNotFoundException | IllegalStateException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Not Found", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/contenedores")
    public ResponseEntity<?> crearContenedor(@RequestBody ContenedorDTO dto, HttpServletRequest request) {
        try {
            Contenedor contenedor = solicitudService.crearContenedor(mapper.toContenedorEntity(dto));
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toContenedorDTO(contenedor));
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/estados")
    public List<EstadoDTO> listarEstados() {
        return solicitudService.listarEstados()
                .stream()
                .map(mapper::toEstadoDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/estados")
    public ResponseEntity<?> crearEstado(@RequestBody EstadoDTO dto) {
        Estado estado = solicitudService.crearEstado(mapper.toEstadoEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toEstadoDTO(estado));
    }

    @PostMapping("/solicitudes/{nroSolicitud}/tarifa")
    public ResponseEntity<?> calcularTarifa(@PathVariable Integer nroSolicitud, HttpServletRequest request) {
        try {
            TarifaSolicitudDTO tarifa = solicitudService.calcularTarifaSolicitud(nroSolicitud);
            return ResponseEntity.ok(tarifa);
        } catch (EntityNotFoundException | IllegalArgumentException | IllegalStateException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "Error al comunicarse con servicios externos: " + e.getMessage(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}