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

    public List<Solicitud> buscarSolicitudes(Integer numero) {
        return (numero != null) 
                ? repoSolicitud.findByNroSolicitud(numero)
                : repoSolicitud.findAll();
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

        nueva.setCliente(clienteDb);
        nueva.setContenedor(contDb);
        
        if (nueva.getEstado() == null) {
             nueva.setEstado(repoEstado.findById(1).orElse(null)); 
        }

        return repoSolicitud.save(nueva);
    }

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
        if (dni != null && estado != null) {
            return repoContenedor.findByCliente_DniClienteAndEstado_DescripcionIgnoreCase(dni, estado);
        }
        if (dni != null) return repoContenedor.findByCliente_DniCliente(dni);
        if (estado != null) return repoContenedor.findByEstado_DescripcionIgnoreCase(estado);
        
        return repoContenedor.findAll();
    }

    public EstadoDTO consultarEstadoContenedor(Integer id) {
        Contenedor c = repoContenedor.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contenedor no hallado"));

        Estado e = c.getEstado();
        if (e == null || e.getContexto() != Contexto.CONTENEDOR) {
            throw new IllegalStateException("Estado inválido o no asignado");
        }
        
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

    public List<Estado> obtenerEstados() {
        return repoEstado.findAll();
    }

    public Estado altaEstado(Estado e) {
        return repoEstado.save(e);
    }

    public TarifaSolicitudDTO cotizarSolicitud(Integer idSolicitud) {
        Solicitud sol = repoSolicitud.findById(idSolicitud)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud inexistente"));

        Contenedor cont = sol.getContenedor();
        if (cont == null) throw new IllegalStateException("Falta contenedor asociado");

        String coordsOrigen = "-64.1833,-31.4167"; 
        String coordsDestino = "-60.64,-32.95";

        DistanciaDTO infoRuta = clienteRutas.consultarDistancia(coordsOrigen, coordsDestino);
        double kms = infoRuta.getKilometros();

        double costoBase = clienteFlota.obtenerCostoBase("BASE", kms, cont.getPesoKg());

        validarCapacidadCamion("AJ345KL", cont.getPesoKg(), cont.getVolumenM3());

        double total = costoBase; 
        
        sol.setCostoReal((float) total);
        repoSolicitud.save(sol);

        TarifaSolicitudDTO cotizacion = new TarifaSolicitudDTO();
        cotizacion.setNroSolicitud(idSolicitud);
        cotizacion.setDistanciaKm(kms);
        cotizacion.setCostoTraslado(costoBase);
        cotizacion.setCostoReal(total);
        
        return cotizacion;
    }

    private void validarDatosMinimos(Solicitud s) {
        if (s.getCliente() == null || s.getCliente().getDniCliente() == null) 
            throw new IllegalArgumentException("Falta cliente");
        if (s.getContenedor() == null || s.getContenedor().getIdContenedor() == null) 
            throw new IllegalArgumentException("Falta contenedor");
    }

    private void validarPropiedadContenedor(Contenedor c, Integer dni) {
        if (c.getCliente() == null || !Objects.equals(c.getCliente().getDniCliente(), dni)) {
            throw new IllegalArgumentException("Contenedor no pertenece al cliente");
        }
    }

    private void validarDisponibilidadContenedor(Integer id) {
        if (repoSolicitud.existsByContenedor_IdContenedorAndEstado_DescripcionIn(id, List.of("ACTIVA", "ACT"))) {
            throw new IllegalArgumentException("Contenedor ya tiene solicitud activa");
        }
    }

    private void actualizarCamposSimples(Solicitud s, SolicitudDTO dto) {
        if (dto.getCostoEstimado() != null) s.setCostoEstimado(dto.getCostoEstimado());
        if (dto.getTiempoEstimado() != null) s.setTiempoEstimado(dto.getTiempoEstimado());
        if (dto.getCostoReal() != null) s.setCostoReal(dto.getCostoReal());
        if (dto.getTiempoReal() != null) s.setTiempoReal(dto.getTiempoReal());
    }

    private void actualizarRelaciones(Solicitud s, SolicitudDTO dto) {
        if (dto.getDniCliente() != null) {
            s.setCliente(repoCliente.findById(dto.getDniCliente()).orElseThrow());
        }
        if (dto.getIdContenedor() != null) {
            s.setContenedor(repoContenedor.findById(dto.getIdContenedor()).orElseThrow());
        }
        if (dto.getIdEstado() != null) {
            s.setEstado(repoEstado.findById(dto.getIdEstado()).orElseThrow());
        }
    }

    private void validarDimensiones(Contenedor c) {
        if (c.getPesoKg() == null || c.getPesoKg() <= 0) throw new IllegalArgumentException("Peso inválido");
        if (c.getVolumenM3() == null || c.getVolumenM3() <= 0) throw new IllegalArgumentException("Volumen inválido");
    }

    private void vincularCliente(Contenedor c) {
        if (c.getCliente() == null || c.getCliente().getDniCliente() == null) 
            throw new IllegalArgumentException("Cliente obligatorio");
        Integer dni = c.getCliente().getDniCliente();
        c.setCliente(repoCliente.findById(dni).orElseThrow(() -> new EntityNotFoundException("Cliente no existe")));
    }

    private void vincularEstado(Contenedor c) {
        if (c.getEstado() != null && c.getEstado().getIdEstado() != null) {
            c.setEstado(repoEstado.findById(c.getEstado().getIdEstado()).orElseThrow());
        }
    }

    private void validarCapacidadCamion(String patente, Float peso, Float volumen) {
        CamionFlotaDTO unidad = clienteFlota.buscarUnidad(patente);
        if (unidad != null) {
            if (peso > unidad.getCapacidadKg()) throw new IllegalArgumentException("Excede peso máximo");
            if (volumen > unidad.getVolumenM3()) throw new IllegalArgumentException("Excede volumen máximo");
        }
    }
}