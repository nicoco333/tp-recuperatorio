package com.tpi.backend.msflota.controller;

import com.tpi.backend.msflota.dto.*;
import com.tpi.backend.msflota.service.FlotaService;
import com.tpi.backend.msflota.util.FlotaMapper;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class FlotaController {

    private final FlotaService flotaService;
    private final FlotaMapper flotaMapper;

    public FlotaController(FlotaService flotaService, FlotaMapper flotaMapper) {
        this.flotaService = flotaService;
        this.flotaMapper = flotaMapper;
    }

    @GetMapping("/camiones")
    public ResponseEntity<?> listarCamiones(@RequestParam(name = "dominioCamion", required = false) String dominioCamion,
                                            @RequestParam(name = "disponibilidad", required = false) Boolean disponibilidad,
                                            HttpServletRequest request) {
        try {
            List<Camion> camiones = flotaService.obtenerCamiones(dominioCamion, disponibilidad);
            List<CamionDTO> dtos = camiones.stream()
                    .map(flotaMapper::toCamionDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            // Atrapa cualquier excepción inesperada y la devuelve como un error 500 personalizado
            ErrorResponseDTO error = new ErrorResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "Ocurrió un error inesperado al listar los camiones: " + e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/camiones/disponibles")
    public List<CamionDTO> obtenerCamionesDisponibles() {
        return flotaService.obtenerCamionesDisponibles()
                .stream()
                .map(flotaMapper::toCamionDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/camiones")
    public ResponseEntity<?> crearCamion(@RequestBody CamionDTO dto, HttpServletRequest request) {
        try {
            Camion camion = flotaService.registrarCamion(flotaMapper.toCamionEntity(dto));
            return ResponseEntity.status(HttpStatus.CREATED).body(flotaMapper.toCamionDTO(camion));
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (EntityNotFoundException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/camiones/{dominio}")
    public ResponseEntity<?> actualizarCamion(@PathVariable String dominio, @RequestBody CamionDTO dto, HttpServletRequest request) {
        try {
            Camion camionActualizado = flotaService.actualizarCamion(dominio, dto);
            return ResponseEntity.ok(flotaMapper.toCamionDTO(camionActualizado));
        } catch (EntityNotFoundException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/transportistas")
    public List<TransportistaDTO> listarTransportistas() {
        return flotaService.listarTransportistas()
                .stream()
                .map(flotaMapper::toTransportistaDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/transportistas")
    public TransportistaDTO crearTransportista(@RequestBody TransportistaDTO dto) {
        Transportista transportista = flotaService.registrarTransportista(
                flotaMapper.toTransportistaEntity(dto)
        );
        return flotaMapper.toTransportistaDTO(transportista);
    }

    @GetMapping("/tarifas")
    public List<TarifaDTO> listarTarifas(@RequestParam(name = "dominioCamion", required = false) String dominioCamion) {
        return flotaService.listarTarifas(dominioCamion)
                .stream()
                .map(flotaMapper::toTarifaDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/tarifas")
    public TarifaDTO crearTarifa(@RequestBody TarifaDTO dto) {
        Tarifa tarifa = flotaMapper.toTarifaEntity(dto);
        Tarifa nueva = flotaService.registrarTarifa(tarifa, dto.getDominioCamion());
        return flotaMapper.toTarifaDTO(nueva);
    }

    // -------------------- CÁLCULO DE COSTO --------------------
    /*@GetMapping("/tarifas/calcular")
    public Double calcularCosto(@RequestParam String tipoContenedor,
                                @RequestParam Double distancia,
                                @RequestParam Double peso) {
        return flotaService.calcularCosto(tipoContenedor, distancia, peso);
    }*/
}
