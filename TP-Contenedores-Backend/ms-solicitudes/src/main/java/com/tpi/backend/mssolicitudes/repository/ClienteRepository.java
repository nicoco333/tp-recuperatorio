package com.tpi.backend.mssolicitudes.repository;

import entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByDniCliente(Integer dniCliente);

    boolean existsByDniCliente(Integer dniCliente);
}
