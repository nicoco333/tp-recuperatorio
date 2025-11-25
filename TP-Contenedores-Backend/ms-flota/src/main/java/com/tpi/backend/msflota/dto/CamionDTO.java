package com.tpi.backend.msflota.dto;

import lombok.Data;

@Data
public class CamionDTO {
    private String dominioCamion;
    private String modelo;
    private Float capacidadKg;
    private Float volumenM3;
    private Boolean disponibilidad;
    private Float consumoPromKm;
    private Float costoTraslado;
    private Integer idTransportista;
}

