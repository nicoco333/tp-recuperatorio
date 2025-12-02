package com.tpi.backend.transporte.util;

import com.tpi.backend.transporte.dto.CamionDTO;
import com.tpi.backend.transporte.dto.TarifaDTO;
import com.tpi.backend.transporte.dto.TransportistaDTO;
import entities.Camion;
import entities.Tarifa;
import entities.Transportista;
import org.springframework.stereotype.Component;

@Component
public class TransporteMapper {

    public CamionDTO toCamionDTO(Camion c) {
        if (c == null) return null;
        CamionDTO dto = new CamionDTO();
        dto.setDominioCamion(c.getDominioCamion());
        dto.setCapacidadKg(c.getCapacidadPesoMax());
        dto.setVolumenM3(c.getCapacidadVolumenMax());
        dto.setDisponibilidad(c.getDisponibilidad());
        dto.setConsumoPromKm(c.getConsumoPromKm());
        dto.setCostoTraslado(c.getCostoTraslado());

        if (c.getTransportista() != null) {
            dto.setIdTransportista(c.getTransportista().getIdTransportista());
        }
        return dto;
    }

    public Camion toCamionEntity(CamionDTO dto) {
        Camion c = new Camion();
        c.setDominioCamion(dto.getDominioCamion());
        c.setCapacidadPesoMax(dto.getCapacidadKg());
        c.setCapacidadVolumenMax(dto.getVolumenM3());
        c.setConsumoPromKm(dto.getConsumoPromKm());
        c.setCostoTraslado(dto.getCostoTraslado());
        c.setDisponibilidad(dto.getDisponibilidad());
        c.setTransportista(null); 
        return c;
    }

    public TransportistaDTO toTransportistaDTO(Transportista t) {
        if (t == null) return null;
        TransportistaDTO dto = new TransportistaDTO();
        dto.setIdTransportista(t.getIdTransportista());
        dto.setNombre(t.getNombre());
        dto.setApellido(t.getApellido());
        dto.setDni(t.getDni());
        dto.setTelefono(t.getTelefono());
        dto.setEmail(t.getEmail());
        dto.setFechaNacimiento(t.getFechaNacimiento());
        dto.setActivo(Boolean.TRUE.equals(t.getActivo()));
        return dto;
    }

    public Transportista toTransportistaEntity(TransportistaDTO dto) {
        if (dto == null) return null;
        Transportista t = new Transportista();
        t.setIdTransportista(dto.getIdTransportista());
        t.setNombre(dto.getNombre());
        t.setApellido(dto.getApellido());
        t.setDni(dto.getDni());
        t.setTelefono(dto.getTelefono());
        t.setEmail(dto.getEmail());
        t.setFechaNacimiento(dto.getFechaNacimiento());
        t.setActivo(dto.isActivo());
        return t;
    }

    public TarifaDTO toTarifaDTO(Tarifa t) {
        TarifaDTO dto = new TarifaDTO();
        dto.setIdTarifa(t.getIdTarifa());
        dto.setTipoTarifa(t.getTipoTarifa());
        dto.setCostoLitroCombustible(t.getCostoLitroCombustible());
        dto.setCargoGestionTramo(t.getCargoGestionTramo());
        if (t.getCamion() != null) {
            dto.setDominioCamion(t.getCamion().getDominioCamion());
        }
        return dto;
    }

    public Tarifa toTarifaEntity(TarifaDTO dto) {
        Tarifa t = new Tarifa();
        t.setIdTarifa(dto.getIdTarifa());
        t.setTipoTarifa(dto.getTipoTarifa());
        t.setCostoLitroCombustible(dto.getCostoLitroCombustible());
        t.setCargoGestionTramo(dto.getCargoGestionTramo());
        return t;
    }
}