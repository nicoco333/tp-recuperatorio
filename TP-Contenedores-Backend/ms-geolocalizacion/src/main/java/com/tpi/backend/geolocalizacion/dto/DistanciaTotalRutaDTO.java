package com.tpi.backend.geolocalizacion.dto;

import lombok.Data;

@Data
public class DistanciaTotalRutaDTO {

    private Integer idRuta;
    private double distanciaTotalKm;
    private int cantidadTramos;
    private long duracionTotalMinutos;
}
