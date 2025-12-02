package com.tpi.backend.geolocalizacion.repository;

import entities.Geolocalizacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeolocalizacionRepository extends JpaRepository<Geolocalizacion, Integer> {}
