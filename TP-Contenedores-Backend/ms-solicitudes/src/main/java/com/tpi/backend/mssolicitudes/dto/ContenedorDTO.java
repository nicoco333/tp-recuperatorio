package com.tpi.backend.mssolicitudes.dto;

import lombok.Data;

/**
 * DTO que representa un contenedor físico.
 */
@Data
public class ContenedorDTO {
    private Integer idContenedor;
    private Float pesoKg;
    private Float volumenM3;
    private Float costoBaseKm;

    private Integer idCliente; // FK (Cliente)
    private Integer idEstado;  // FK (Estado)
}
