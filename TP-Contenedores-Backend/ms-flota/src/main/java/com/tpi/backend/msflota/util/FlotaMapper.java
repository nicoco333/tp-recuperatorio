package com.tpi.backend.msflota.util;

import com.tpi.backend.msflota.dto.CamionDTO;
import com.tpi.backend.msflota.dto.TarifaDTO;
import com.tpi.backend.msflota.dto.TransportistaDTO;
import entities.Camion;
import entities.Tarifa;
import entities.Transportista;
import org.springframework.stereotype.Component;

@Component
public class FlotaMapper {

    // ---------- CAMION ----------
    public CamionDTO toCamionDTO(Camion e) {
        if (e == null) return null;
        CamionDTO dto = new CamionDTO();
        dto.setDominioCamion(e.getDominioCamion());
        dto.setCapacidadKg(e.getCapacidadPesoMax());
        dto.setVolumenM3(e.getCapacidadVolumenMax());
        dto.setDisponibilidad(e.getDisponibilidad());
        dto.setConsumoPromKm(e.getConsumoPromKm());
        dto.setCostoTraslado(e.getCostoTraslado());

        // ESTA ES LA CORRECCIÓN CLAVE
        if (e.getTransportista() != null) {
            dto.setIdTransportista(e.getTransportista().getIdTransportista());
        } else {
            dto.setIdTransportista(null); // Explícitamente nulo si no hay transportista
        }

        return dto;
    }


    public Camion toCamionEntity(CamionDTO dto) {
        Camion e = new Camion();

        e.setDominioCamion(dto.getDominioCamion());

        // Campos obligatorios de la entidad
        e.setCapacidadPesoMax(dto.getCapacidadKg());
        e.setCapacidadVolumenMax(dto.getVolumenM3());
        e.setConsumoPromKm(dto.getConsumoPromKm());
        e.setCostoTraslado(dto.getCostoTraslado());
        e.setDisponibilidad(dto.getDisponibilidad());

        // Transportista:
        e.setTransportista(null);

        return e;
    }


    // ---------- TRANSPORTISTA ----------
    public TransportistaDTO toTransportistaDTO(Transportista e) {
        if (e == null) {
            return null;
        }

        TransportistaDTO dto = new TransportistaDTO();
        dto.setIdTransportista(e.getIdTransportista());
        dto.setNombre(e.getNombre());
        dto.setApellido(e.getApellido());
        dto.setDni(e.getDni());
        dto.setTelefono(e.getTelefono());
        dto.setEmail(e.getEmail());
        dto.setFechaNacimiento(e.getFechaNacimiento());
        dto.setActivo(Boolean.TRUE.equals(e.getActivo()));

        return dto;
    }

    public Transportista toTransportistaEntity(TransportistaDTO dto) {
        if (dto == null) {
            return null;
        }

        Transportista e = new Transportista();
        e.setIdTransportista(dto.getIdTransportista());
        e.setNombre(dto.getNombre());
        e.setApellido(dto.getApellido());
        e.setDni(dto.getDni());
        e.setTelefono(dto.getTelefono());
        e.setEmail(dto.getEmail());
        e.setFechaNacimiento(dto.getFechaNacimiento());
        e.setActivo(dto.isActivo()); // boolean -> Boolean (autoboxing)

        return e;
    }


    // ---------- TARIFA ----------
    public TarifaDTO toTarifaDTO(Tarifa e) {
        TarifaDTO dto = new TarifaDTO();
        dto.setIdTarifa(e.getIdTarifa());
        dto.setTipoTarifa(e.getTipoTarifa());
        dto.setCostoLitroCombustible(e.getCostoLitroCombustible());
        dto.setCargoGestionTramo(e.getCargoGestionTramo());
        if (e.getCamion() != null) {
            dto.setDominioCamion(e.getCamion().getDominioCamion());
        }
        return dto;
    }

    public Tarifa toTarifaEntity(TarifaDTO dto) {
        Tarifa e = new Tarifa();
        e.setIdTarifa(dto.getIdTarifa());
        e.setTipoTarifa(dto.getTipoTarifa());
        e.setCostoLitroCombustible(dto.getCostoLitroCombustible());
        e.setCargoGestionTramo(dto.getCargoGestionTramo());
        // No seteamos Camion completo aquí, se puede resolver luego en el Service
        return e;
    }
}
