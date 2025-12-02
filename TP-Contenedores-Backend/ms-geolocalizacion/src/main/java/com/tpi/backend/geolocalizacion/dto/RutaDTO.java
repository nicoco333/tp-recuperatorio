package com.tpi.backend.geolocalizacion.dto;

import lombok.Data;
import java.util.List;

@Data
public class RutaDTO {
    private Integer idRuta;
    private Integer nroSolicitud;
    private Integer cantTramos;
    private Integer cantDepositos;

    // Lista de tramos asociados
    private List<TramoDTO> tramos;
}
