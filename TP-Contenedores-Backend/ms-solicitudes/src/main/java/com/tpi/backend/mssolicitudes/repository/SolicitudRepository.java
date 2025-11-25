package com.tpi.backend.mssolicitudes.repository;

import entities.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {
    boolean existsByContenedor_IdContenedorAndEstado_DescripcionIn(
            Integer idContenedor,
            List<String> descripcionesEstado
    );

    List<Solicitud> findByNroSolicitud(Integer nroSolicitud);
}
