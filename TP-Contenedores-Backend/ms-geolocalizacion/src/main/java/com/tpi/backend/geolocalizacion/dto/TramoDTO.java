package com.tpi.backend.geolocalizacion.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TramoDTO {
    private Integer idTramo;
    private Integer idRuta;
    private Integer origenGeo;
    private Integer destinoGeo;
    private Integer origenDepositoId;
    private Integer destinoDepositoId;
    private Integer tipoTramo;
    private Integer idEstado;

    private Integer orden;

    private LocalDateTime fechaHoraInicioEstimada;
    private LocalDateTime fechaHoraFinEstimada;
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;

    private Float costoAproximado;
    private Float costoReal;

    private String dominioCamion;
}
