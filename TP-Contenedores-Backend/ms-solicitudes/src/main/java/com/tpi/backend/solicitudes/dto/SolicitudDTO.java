package com.tpi.backend.solicitudes.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO para representar una solicitud de transporte.
 */
@Data
public class SolicitudDTO {
    private Integer nroSolicitud;
    private Float costoEstimado;
    private Integer tiempoEstimado;
    private Float costoReal;
    private Integer tiempoReal;
    private LocalDateTime fechaCreacion;

    private Integer dniCliente;   // FK Cliente
    private Integer idContenedor; // FK Contenedor
    private Integer idEstado;     // FK Estado
}
