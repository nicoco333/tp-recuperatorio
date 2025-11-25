package com.tpi.backend.msrutas.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TiemposTramoDTO {
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
}

