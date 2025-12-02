package com.tpi.backend.geolocalizacion.dto.distanciageo;

import lombok.Data;

@Data
public class DistanciaDTO {
    private String origen;
    private String destino;
    private double kilometros;
    private String duracionTexto;
    private long duracionMinutos;
}