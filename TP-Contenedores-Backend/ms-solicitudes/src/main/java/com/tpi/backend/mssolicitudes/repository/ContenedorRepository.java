package com.tpi.backend.mssolicitudes.repository;

import entities.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Integer> {
    List<Contenedor> findByEstado_DescripcionIgnoreCase(String descripcion);

    List<Contenedor> findByCliente_DniCliente(Integer dniCliente);

    List<Contenedor> findByCliente_DniClienteAndEstado_DescripcionIgnoreCase(
            Integer dniCliente,
            String descripcionEstado
    );
}
