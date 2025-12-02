package com.tpi.backend.geolocalizacion.repository;

import entities.Camion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CamionRepository extends JpaRepository<Camion, String> {}