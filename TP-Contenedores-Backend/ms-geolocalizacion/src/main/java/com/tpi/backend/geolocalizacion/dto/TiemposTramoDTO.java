package com.tpi.backend.geolocalizacion.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TiemposTramoDTO {
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
}

