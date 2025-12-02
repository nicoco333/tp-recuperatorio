package com.tpi.backend.msflota.repository;

import entities.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {
    List<Tarifa> findByCamion_DominioCamionContainingIgnoreCase(String dominioCamion);
    Tarifa findByTipoTarifa(String tipoTarifa);
}
