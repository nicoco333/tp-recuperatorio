package com.tpi.backend.msflota.repository;

import entities.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportistaRepository extends JpaRepository<Transportista, Integer> {
    boolean existsByDni(String dni);
}
