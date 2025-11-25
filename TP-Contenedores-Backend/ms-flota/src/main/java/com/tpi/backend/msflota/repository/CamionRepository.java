package com.tpi.backend.msflota.repository;

import entities.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CamionRepository extends JpaRepository<Camion, String> {
    List<Camion> findByDisponibilidad(Boolean disponibilidad);

    List<Camion> findByDominioCamionContainingIgnoreCase(String dominioCamion);

    List<Camion> findByDominioCamionContainingIgnoreCaseAndDisponibilidad(
            String dominioCamion,
            Boolean disponibilidad
    );
}

