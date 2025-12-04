package com.tpi.backend.solicitudes.service;

import com.tpi.backend.solicitudes.dto.*;
import entities.*;
import com.tpi.backend.solicitudes.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

import com.tpi.backend.solicitudes.client.TransporteClient;
import com.tpi.backend.solicitudes.client.GeoClient;

@Service
public class SolicitudService {

    private final SolicitudRepository repoSolicitud;
    private final ClienteRepository repoCliente;
    private final ContenedorRepository repoContenedor;
    private final EstadoRepository repoEstado;
    private final GeoClient clienteLocalizacion;
    private final TransporteClient clienteTransporte;

    public SolicitudService(SolicitudRepository repoSolicitud,
                            ClienteRepository repoCliente,
                            ContenedorRepository repoContenedor,
                            EstadoRepository repoEstado,
                            GeoClient clienteLocalizacion,
                            TransporteClient clienteTransporte) {
        this.repoSolicitud = repoSolicitud;
        this.repoCliente = repoCliente;
        this.repoContenedor = repoContenedor;
        this.repoEstado = repoEstado;
        this.clienteLocalizacion = clienteLocalizacion;
        this.clienteTransporte = clienteTransporte;
    }

    public List<Solicitud> buscarSolicitudes(Integer numero, Integer dniCliente) {
        if (numero != null) return repoSolicitud.findByNroSolicitud(numero);
        if (dniCliente != null) return repoSolicitud.findByCliente_DniCliente(dniCliente);
        return repoSolicitud.findAll();
    }

    // --- AQUÍ ESTÁ EL CAMBIO ---
    public void actualizarProgreso(Integer idSolicitud, 
                                   Double deltaCostoEst, Integer deltaTiempoEst, 
                                   Double deltaCostoReal, Integer deltaTiempoReal, 
                                   String descripcionEstado) {
        
        Solicitud sol = repoSolicitud.findById(idSolicitud)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + idSolicitud));

        // 1. Acumular Estimados
        if (deltaCostoEst != null) {
            float actual = sol.getCostoEstimado() != null ? sol.getCostoEstimado() : 0f;
            sol.setCostoEstimado(actual + deltaCostoEst.floatValue());
        }
        if (deltaTiempoEst != null) {
            int actual = sol.getTiempoEstimado() != null ? sol.getTiempoEstimado() : 0;
            sol.setTiempoEstimado(actual + deltaTiempoEst);
        }

        // 2. Acumular Reales
        if (deltaCostoReal != null) {
            float actual = sol.getCostoReal() != null ? sol.getCostoReal() : 0f;
            sol.setCostoReal(actual + deltaCostoReal.floatValue());
        }
        if (deltaTiempoReal != null) {
            int actual = sol.getTiempoReal() != null ? sol.getTiempoReal() : 0;
            sol.setTiempoReal(actual + deltaTiempoReal);
        }

        // 3. Actualizar Estado y Liberar Contenedor
        if (descripcionEstado != null && !descripcionEstado.isBlank()) {
            Estado nuevoEstado = repoEstado.findAll().stream()
                    .filter(e -> e.getDescripcion().equalsIgnoreCase(descripcionEstado)
                            && e.getContexto() == Contexto.SOLICITUD)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Estado inválido: " + descripcionEstado));
            
            sol.setEstado(nuevoEstado);

            // --- LÓGICA DE LIBERACIÓN DE CONTENEDOR ---
            if ("FINALIZADA".equalsIgnoreCase(descripcionEstado)) {
                Contenedor c = sol.getContenedor();
                if (c != null) {
                    // Volver a estado DISPONIBLE (ID 1)
                    Estado disponible = repoEstado.findById(1)
                            .orElseThrow(() -> new IllegalStateException("Estado DISPONIBLE no encontrado"));
                    c.setEstado(disponible);
                    repoContenedor.save(c);
                }
            }
            // ------------------------------------------
        }

        repoSolicitud.save(sol);
    }

    public Solicitud registrarSolicitud(Solicitud nueva) {
        validarDatosMinimos(nueva);

        Integer dni = nueva.getCliente().getDniCliente();
        Cliente clienteDb = repoCliente.findById(dni)
                .orElseThrow(() -> new EntityNotFoundException("Cliente inexistente: " + dni));

        Integer idCont = nueva.getContenedor().getIdContenedor();
        Contenedor contDb = repoContenedor.findById(idCont)
                .orElseThrow(() -> new EntityNotFoundException("Contenedor inexistente: " + idCont));

        validarPropiedadContenedor(contDb, dni);
        validarDisponibilidadContenedor(idCont);

        if (contDb.getEstado().getIdEstado() != 1) { 
             throw new IllegalArgumentException("El contenedor seleccionado no está disponible (ID Estado != 1).");
        }
        Estado estadoOcupado = repoEstado.findById(6)
                .orElseThrow(() -> new IllegalStateException("Estado ID 6 (CONTENEDOR OCUPADO) no encontrado"));
        contDb.setEstado(estadoOcupado);
        repoContenedor.save(contDb);

        nueva.setCliente(clienteDb);
        nueva.setContenedor(contDb);
        
        // Inicializar en 0
        nueva.setCostoReal(0f); 
        nueva.setCostoEstimado(0f);
        nueva.setTiempoEstimado(0);
        nueva.setTiempoReal(0);

        if (nueva.getEstado() == null) {
             nueva.setEstado(repoEstado.findById(2).orElse(null)); 
        }

        return repoSolicitud.save(nueva);
    }

    // ... (El resto de los métodos modificarSolicitud, obtenerClientes, etc. siguen igual) ...
    public Solicitud modificarSolicitud(Integer id, SolicitudDTO datos) {
        Solicitud actual = repoSolicitud.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se halló la solicitud " + id));
        actualizarCamposSimples(actual, datos);
        actualizarRelaciones(actual, datos);
        return repoSolicitud.save(actual);
    }

    public List<Cliente> obtenerClientes(Integer dni) {
        return (dni != null) ? repoCliente.findByDniCliente(dni) : repoCliente.findAll();
    }

    public Cliente altaCliente(Cliente c) {
        if (c.getDniCliente() == null) throw new IllegalArgumentException("DNI requerido");
        if (repoCliente.existsByDniCliente(c.getDniCliente())) {
            throw new IllegalArgumentException("El cliente ya existe");
        }
        return repoCliente.save(c);
    }

    public List<Contenedor> filtrarContenedores(String estado, Integer dni) {
        if (dni != null && estado != null) return repoContenedor.findByCliente_DniClienteAndEstado_DescripcionIgnoreCase(dni, estado);
        if (dni != null) return repoContenedor.findByCliente_DniCliente(dni);
        if (estado != null) return repoContenedor.findByEstado_DescripcionIgnoreCase(estado);
        return repoContenedor.findAll();
    }

    public EstadoDTO consultarEstadoContenedor(Integer id) {
        Contenedor c = repoContenedor.findById(id).orElseThrow(() -> new EntityNotFoundException("Contenedor no hallado"));
        Estado e = c.getEstado();
        if (e == null || e.getContexto() != Contexto.CONTENEDOR) throw new IllegalStateException("Estado inválido");
        EstadoDTO dto = new EstadoDTO();
        dto.setIdEstado(e.getIdEstado());
        dto.setContexto(e.getContexto().name());
        dto.setDescripcion(e.getDescripcion());
        return dto;
    }

    public Contenedor altaContenedor(Contenedor nuevo) {
        validarDimensiones(nuevo);
        vincularCliente(nuevo);
        vincularEstado(nuevo);
        return repoContenedor.save(nuevo);
    }

    public List<Estado> obtenerEstados() { return repoEstado.findAll(); }
    public Estado altaEstado(Estado e) { return repoEstado.save(e); }


    private void validarDatosMinimos(Solicitud s) {
        if (s.getCliente() == null || s.getCliente().getDniCliente() == null) throw new IllegalArgumentException("Falta cliente");
        if (s.getContenedor() == null || s.getContenedor().getIdContenedor() == null) throw new IllegalArgumentException("Falta contenedor");
    }

    private void validarPropiedadContenedor(Contenedor c, Integer dni) {
        if (c.getCliente() == null || !Objects.equals(c.getCliente().getDniCliente(), dni)) throw new IllegalArgumentException("Contenedor no pertenece al cliente");
    }

    private void validarDisponibilidadContenedor(Integer id) {
        if (repoSolicitud.existsByContenedor_IdContenedorAndEstado_DescripcionIn(id, List.of("ACTIVA", "ACT", "EN CURSO"))) throw new IllegalArgumentException("Contenedor ya tiene solicitud activa");
    }

    private void actualizarCamposSimples(Solicitud s, SolicitudDTO dto) {
        if (dto.getCostoEstimado() != null) s.setCostoEstimado(dto.getCostoEstimado());
        if (dto.getTiempoEstimado() != null) s.setTiempoEstimado(dto.getTiempoEstimado());
        if (dto.getCostoReal() != null) s.setCostoReal(dto.getCostoReal());
        if (dto.getTiempoReal() != null) s.setTiempoReal(dto.getTiempoReal());
    }

    private void actualizarRelaciones(Solicitud s, SolicitudDTO dto) {
        if (dto.getDniCliente() != null) s.setCliente(repoCliente.findById(dto.getDniCliente()).orElseThrow());
        if (dto.getIdContenedor() != null) s.setContenedor(repoContenedor.findById(dto.getIdContenedor()).orElseThrow());
        if (dto.getIdEstado() != null) s.setEstado(repoEstado.findById(dto.getIdEstado()).orElseThrow());
    }

    private void validarDimensiones(Contenedor c) {
        if (c.getPesoKg() == null || c.getPesoKg() <= 0) throw new IllegalArgumentException("Peso inválido");
        if (c.getVolumenM3() == null || c.getVolumenM3() <= 0) throw new IllegalArgumentException("Volumen inválido");
    }

    private void vincularCliente(Contenedor c) {
        if (c.getCliente() == null || c.getCliente().getDniCliente() == null) throw new IllegalArgumentException("Cliente obligatorio");
        Integer dni = c.getCliente().getDniCliente();
        c.setCliente(repoCliente.findById(dni).orElseThrow(() -> new EntityNotFoundException("Cliente no existe")));
    }

    private void vincularEstado(Contenedor c) {
        if (c.getEstado() != null && c.getEstado().getIdEstado() != null) c.setEstado(repoEstado.findById(c.getEstado().getIdEstado()).orElseThrow());
    }
}