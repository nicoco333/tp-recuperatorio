package com.tpi.backend.msrutas.repository;

import entities.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoRepository extends JpaRepository<Estado, Integer> {
    Optional<Estado> findByDescripcion(String descripcion);
}
