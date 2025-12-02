package com.tpi.backend.transporte.service;

import com.tpi.backend.transporte.dto.CamionDTO;
import entities.Camion;
import entities.Tarifa;
import entities.Transportista;
import com.tpi.backend.transporte.repository.CamionRepository;
import com.tpi.backend.transporte.repository.TarifaRepository;
import com.tpi.backend.transporte.repository.TransportistaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransporteService {

    private final CamionRepository repoCamion;
    private final TarifaRepository repoTarifa;
    private final TransportistaRepository repoTransportista;

    public TransporteService(CamionRepository repoCamion,
                             TarifaRepository repoTarifa,
                             TransportistaRepository repoTransportista) {
        this.repoCamion = repoCamion;
        this.repoTarifa = repoTarifa;
        this.repoTransportista = repoTransportista;
    }

    public List<Camion> buscarUnidades(String patente, Boolean soloDisponibles) {
        if (patente != null && !patente.isBlank()) {
            if (Boolean.TRUE.equals(soloDisponibles)) {
                return repoCamion.findByDominioCamionContainingIgnoreCaseAndDisponibilidad(patente, true);
            }
            return repoCamion.findByDominioCamionContainingIgnoreCase(patente);
        }
        
        if (Boolean.TRUE.equals(soloDisponibles)) {
            return repoCamion.findByDisponibilidad(true);
        }
        
        return repoCamion.findAll();
    }

    public Camion altaCamion(Camion unidad) {
        String dominio = unidad.getDominioCamion();
        
        if (dominio == null || dominio.trim().isEmpty()) {
            throw new IllegalArgumentException("Patente requerida");
        }
        
        if (repoCamion.existsById(dominio)) {
            throw new IllegalArgumentException("Patente duplicada: " + dominio);
        }

        asignarTransportistaSiExiste(unidad);
        
        return repoCamion.save(unidad);
    }

    public List<Camion> listarDisponibles() {
        return repoCamion.findByDisponibilidad(true);
    }

    public Camion modificarCamion(String patente, CamionDTO datos) {
        Camion actual = repoCamion.findById(patente)
                .orElseThrow(() -> new EntityNotFoundException("Unidad no hallada: " + patente));

        validarConsistenciaPatente(patente, datos.getDominioCamion());
        actualizarDatosCamion(actual, datos);
        actualizarTransportista(actual, datos.getIdTransportista());

        return repoCamion.save(actual);
    }

    public List<Transportista> obtenerTransportistas() {
        return repoTransportista.findAll();
    }

    public Transportista altaTransportista(Transportista t) {
        validarTransportista(t);
        if (t.getActivo() == null) t.setActivo(true);
        return repoTransportista.save(t);
    }

    public List<Tarifa> consultarTarifas(String patente) {
        if (patente != null && !patente.isBlank()) {
            return repoTarifa.findByCamion_DominioCamionContainingIgnoreCase(patente);
        }
        return repoTarifa.findAll();
    }

    public Tarifa nuevaTarifa(Tarifa t, String patente) {
        validarTarifa(t);
        
        Camion c = repoCamion.findById(patente)
                .orElseThrow(() -> new IllegalArgumentException("Camión inexistente"));
        
        t.setCamion(c);
        t.setIdTarifa(null);
        
        return repoTarifa.save(t);
    }

    // --- Métodos privados refactorizados ---

    private void asignarTransportistaSiExiste(Camion c) {
        if (c.getTransportista() != null && c.getTransportista().getIdTransportista() != null) {
            Integer id = c.getTransportista().getIdTransportista();
            c.setTransportista(repoTransportista.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Transportista erróneo")));
        }
    }

    private void validarConsistenciaPatente(String original, String nueva) {
        if (nueva != null && !nueva.isBlank() && !nueva.equals(original)) {
            throw new IllegalArgumentException("No se puede cambiar la patente");
        }
    }

    private void actualizarDatosCamion(Camion c, CamionDTO dto) {
        Optional.ofNullable(dto.getCapacidadKg()).ifPresent(c::setCapacidadPesoMax);
        Optional.ofNullable(dto.getVolumenM3()).ifPresent(c::setCapacidadVolumenMax);
        Optional.ofNullable(dto.getDisponibilidad()).ifPresent(c::setDisponibilidad);
        Optional.ofNullable(dto.getConsumoPromKm()).ifPresent(c::setConsumoPromKm);
        Optional.ofNullable(dto.getCostoTraslado()).ifPresent(c::setCostoTraslado);
    }

    private void actualizarTransportista(Camion c, Integer idTrans) {
        if (idTrans != null) {
            c.setTransportista(repoTransportista.findById(idTrans)
                    .orElseThrow(() -> new EntityNotFoundException("Transportista no existe")));
        }
    }

    private void validarTransportista(Transportista t) {
        if (t.getNombre() == null || t.getApellido() == null || t.getDni() == null) {
            throw new IllegalArgumentException("Datos personales incompletos");
        }
        if (repoTransportista.existsByDni(t.getDni())) {
            throw new IllegalArgumentException("DNI ya registrado");
        }
    }

    private void validarTarifa(Tarifa t) {
        if (t.getTipoTarifa() == null) throw new IllegalArgumentException("Falta tipo tarifa");
        if (t.getCostoLitroCombustible() == null || t.getCostoLitroCombustible() <= 0) 
            throw new IllegalArgumentException("Costo combustible inválido");
    }
    
    // Método de cálculo para exponer al endpoint
    public Double estimarCosto(String tipo, Double km, Double peso) {
        // Lógica de ejemplo: Costo = (Km * ValorKm) + (Peso * ValorPeso)
        // Podrías buscar una tarifa base en DB si quisieras
        double base = 100.0; 
        return base + (km * 1.5) + (peso * 0.05);
    }
}