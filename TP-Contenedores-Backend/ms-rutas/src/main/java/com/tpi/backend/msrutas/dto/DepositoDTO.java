package com.tpi.backend.msrutas.dto;

import lombok.Data;

@Data
public class DepositoDTO {
    private Integer idDeposito;
    private String nombre;
    private String direccion;
    private Float latitud;
    private Float longitud;
    private String tipo; // central, intermedio, destino
    private Float costoEstadiaDiaria;
}
