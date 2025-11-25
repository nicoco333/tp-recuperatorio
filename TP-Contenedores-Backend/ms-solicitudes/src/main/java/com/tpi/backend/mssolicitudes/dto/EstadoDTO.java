package com.tpi.backend.mssolicitudes.dto;

import lombok.Data;

/**
 * DTO que representa un estado (de solicitud o contenedor).
 */
@Data
public class EstadoDTO {
    private Integer idEstado;
    private String contexto;
    private String descripcion;
}
