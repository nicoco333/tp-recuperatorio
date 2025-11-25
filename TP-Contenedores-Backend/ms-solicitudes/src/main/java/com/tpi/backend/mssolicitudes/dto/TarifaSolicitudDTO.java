package com.tpi.backend.mssolicitudes.dto;

import lombok.Data;

@Data
public class TarifaSolicitudDTO {
    private Integer nroSolicitud;

    private Double distanciaKm;
    private Double costoTraslado;   // costo calculado del tramo/traslados
    private Double costoEstadia;    // si costo_estadia_diaria * días
    private Double cargosGestion;   // cargo_gestion_tramo u otros

    private Double costoReal;       // suma final
 }