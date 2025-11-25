package com.tpi.backend.msrutas.dto;

import lombok.Data;

@Data
public class DistanciaTotalRutaDTO {

    private Integer idRuta;
    private double distanciaTotalKm;
    private int cantidadTramos;
    private long duracionTotalMinutos;
}
