package com.tpi.backend.msflota.dto;

import lombok.Data;

@Data
public class TarifaDTO {
    private Integer idTarifa;
    private String tipoTarifa;
    private Float costoLitroCombustible;
    private Float cargoGestionTramo;
    private String dominioCamion;
}
