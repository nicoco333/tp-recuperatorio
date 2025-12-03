package com.tpi.backend.solicitudes.repository;

import entities.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {
    
    // Filtro por DNI del cliente
    List<Solicitud> findByCliente_DniCliente(Integer dniCliente);

    List<Solicitud> findByNroSolicitud(Integer nroSolicitud);

    boolean existsByContenedor_IdContenedorAndEstado_DescripcionIn(
            Integer idContenedor,
            List<String> descripcionesEstado
    );
}