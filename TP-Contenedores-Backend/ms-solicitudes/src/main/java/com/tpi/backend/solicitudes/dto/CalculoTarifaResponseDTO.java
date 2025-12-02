package com.tpi.backend.solicitudes.dto;

import lombok.Data;

@Data
public class CalculoTarifaResponseDTO {
    private Double tarifaFinal;
    private String detalle;
    private boolean valido;
    private String mensaje;
}

