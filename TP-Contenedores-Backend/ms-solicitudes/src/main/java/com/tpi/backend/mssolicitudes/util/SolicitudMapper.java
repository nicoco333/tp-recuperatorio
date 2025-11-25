package com.tpi.backend.mssolicitudes.util;

import com.tpi.backend.mssolicitudes.dto.*;
import entities.*;
import org.springframework.stereotype.Component;

@Component
public class SolicitudMapper {

    // ---------- SOLICITUD ----------
    public SolicitudDTO toSolicitudDTO(Solicitud e) {
        SolicitudDTO dto = new SolicitudDTO();
        dto.setNroSolicitud(e.getNroSolicitud());
        dto.setCostoEstimado(e.getCostoEstimado());
        // entidad guarda tiempo como Integer, DTO usa Float -> convertir
        dto.setTiempoEstimado(e.getTiempoEstimado() != null ? e.getTiempoEstimado(): null);
        dto.setCostoReal(e.getCostoReal());
        dto.setTiempoReal(e.getTiempoReal() != null ? e.getTiempoReal() : null);
        dto.setFechaCreacion(e.getFechaCreacion());
        dto.setDniCliente(e.getCliente() != null ? e.getCliente().getDniCliente() : null);
        dto.setIdContenedor(e.getContenedor() != null ? e.getContenedor().getIdContenedor() : null);
        dto.setIdEstado(e.getEstado() != null ? e.getEstado().getIdEstado() : null);
        return dto;
    }

    public Solicitud toSolicitudEntity(SolicitudDTO dto) {
        Solicitud e = new Solicitud();
        e.setNroSolicitud(dto.getNroSolicitud());
        e.setCostoEstimado(dto.getCostoEstimado());
        // DTO tiene Float para tiempos; entidad usa Integer -> convertir
        e.setTiempoEstimado(dto.getTiempoEstimado() != null ? dto.getTiempoEstimado().intValue() : null);
        e.setCostoReal(dto.getCostoReal());
        e.setTiempoReal(dto.getTiempoReal() != null ? dto.getTiempoReal().intValue() : null);
        e.setFechaCreacion(dto.getFechaCreacion());
        // relaciones por IDs
        if (dto.getDniCliente() != null) {
            Cliente c = new Cliente();
            c.setDniCliente(dto.getDniCliente());
            e.setCliente(c);
        }
        if (dto.getIdContenedor() != null) {
            Contenedor cont = new Contenedor();
            cont.setIdContenedor(dto.getIdContenedor());
            e.setContenedor(cont);
        }
        if (dto.getIdEstado() != null) {
            Estado estado = new Estado();
            estado.setIdEstado(dto.getIdEstado());
            e.setEstado(estado);
        }
        return e;
    }

    // ---------- CLIENTE ----------
    public ClienteDTO toClienteDTO(Cliente e) {
        ClienteDTO dto = new ClienteDTO();
        dto.setDniCliente(e.getDniCliente());
        dto.setNombre(e.getNombre());
        dto.setApellido(e.getApellido());
        dto.setTelefono(e.getTelefono());
        return dto;
    }

    public Cliente toClienteEntity(ClienteDTO dto) {
        Cliente e = new Cliente();
        e.setDniCliente(dto.getDniCliente());
        e.setNombre(dto.getNombre());
        e.setApellido(dto.getApellido());
        e.setTelefono(dto.getTelefono());
        return e;
    }

    // ---------- CONTENEDOR ----------
    public ContenedorDTO toContenedorDTO(Contenedor e) {
        ContenedorDTO dto = new ContenedorDTO();
        dto.setIdContenedor(e.getIdContenedor());
        dto.setPesoKg(e.getPesoKg());
        dto.setVolumenM3(e.getVolumenM3());
        dto.setCostoBaseKm(e.getCostoBaseKm());
        dto.setIdCliente(e.getCliente() != null ? e.getCliente().getDniCliente() : null);
        dto.setIdEstado(e.getEstado() != null ? e.getEstado().getIdEstado() : null);
        return dto;
    }

    public Contenedor toContenedorEntity(ContenedorDTO dto) {
        Contenedor e = new Contenedor();
        e.setIdContenedor(dto.getIdContenedor());
        e.setPesoKg(dto.getPesoKg());
        e.setVolumenM3(dto.getVolumenM3());
        e.setCostoBaseKm(dto.getCostoBaseKm());
        if (dto.getIdCliente() != null) {
            Cliente c = new Cliente();
            c.setDniCliente(dto.getIdCliente());
            e.setCliente(c);
        }
        if (dto.getIdEstado() != null) {
            Estado st = new Estado();
            st.setIdEstado(dto.getIdEstado());
            e.setEstado(st);
        }
        return e;
    }

    // ---------- ESTADO ----------
    public EstadoDTO toEstadoDTO(Estado e) {
        EstadoDTO dto = new EstadoDTO();
        dto.setIdEstado(e.getIdEstado());
        dto.setContexto(e.getContexto() != null ? e.getContexto().name() : null);
        dto.setDescripcion(e.getDescripcion());
        return dto;
    }

    public Estado toEstadoEntity(EstadoDTO dto) {
        Estado e = new Estado();
        e.setIdEstado(dto.getIdEstado());
        if (dto.getContexto() != null) {
            try {
                e.setContexto(Contexto.valueOf(dto.getContexto()));
            } catch (IllegalArgumentException ex) {
                // si el valor no coincide, dejar null o manejar según convenga
                e.setContexto(null);
            }
        }
        e.setDescripcion(dto.getDescripcion());
        return e;
    }
}
