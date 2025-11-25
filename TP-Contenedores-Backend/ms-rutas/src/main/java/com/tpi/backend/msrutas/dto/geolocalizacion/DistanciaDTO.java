package com.tpi.backend.msrutas.dto.geolocalizacion;

import lombok.Data;

@Data
public class DistanciaDTO {
    private String origen;
    private String destino;
    private double kilometros;
    private String duracionTexto;
    private long duracionMinutos;
}