package com.tpi.backend.geolocalizacion.repository;

import entities.Tramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Integer> {
    List<Tramo> findByRuta_IdRuta(Integer idRuta);

    // Buscar tramos por nroSolicitud a través de la relación ruta -> solicitud -> nroSolicitud
    List<Tramo> findByRuta_Solicitud_NroSolicitudOrderByOrdenAsc(Integer nroSolicitud);

    Optional<Tramo> findFirstByRuta_Solicitud_NroSolicitudOrderByOrdenAsc(Integer nroSolicitud);
}