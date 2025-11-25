package com.tpi.backend.mssolicitudes.service;

import com.tpi.backend.mssolicitudes.dto.*;
import entities.*;
import com.tpi.backend.mssolicitudes.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import com.tpi.backend.mssolicitudes.client.FlotaClient;
import com.tpi.backend.mssolicitudes.client.RutasClient;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;
    private final EstadoRepository estadoRepository;
    private final RutasClient rutasClient;
    private final FlotaClient flotaClient;

    public SolicitudService(SolicitudRepository solicitudRepository,
                            ClienteRepository clienteRepository,
                            ContenedorRepository contenedorRepository,
                            EstadoRepository estadoRepository,
                            RutasClient rutasClient,
                            FlotaClient flotaClient) {
        this.solicitudRepository = solicitudRepository;
        this.clienteRepository = clienteRepository;
        this.contenedorRepository = contenedorRepository;
        this.estadoRepository = estadoRepository;
        this.rutasClient = rutasClient;
        this.flotaClient = flotaClient;
    }

    public List<Solicitud> listarSolicitudes(Integer nroSolicitud) {
        if (nroSolicitud != null) {
            return solicitudRepository.findByNroSolicitud(nroSolicitud);
        }
        return solicitudRepository.findAll();
    }

    public Solicitud crearSolicitud(Solicitud solicitud) {
        if (solicitud.getCliente() == null || solicitud.getCliente().getDniCliente() == null) {
            throw new IllegalArgumentException("El dni_cliente es obligatorio.");
        }
        Integer dniCliente = solicitud.getCliente().getDniCliente();
        var cliente = clienteRepository.findById(dniCliente)
                .orElseThrow(() -> new EntityNotFoundException("No existe un cliente registrado con DNI " + dniCliente));

        if (solicitud.getContenedor() == null || solicitud.getContenedor().getIdContenedor() == null) {
            throw new IllegalArgumentException("El id_contenedor es obligatorio.");
        }
        Integer idContenedor = solicitud.getContenedor().getIdContenedor();
        var contenedor = contenedorRepository.findById(idContenedor)
                .orElseThrow(() -> new EntityNotFoundException("No existe un contenedor con id " + idContenedor));

        if (contenedor.getCliente() == null || contenedor.getCliente().getDniCliente() == null || !contenedor.getCliente().getDniCliente().equals(dniCliente)) {
            throw new IllegalArgumentException("El contenedor " + idContenedor + " no pertenece al cliente con DNI " + dniCliente);
        }

        List<String> estadosBloqueantes = List.of("ACTIVA", "ACT");
        boolean existeSolicitudActiva = solicitudRepository
                .existsByContenedor_IdContenedorAndEstado_DescripcionIn(idContenedor, estadosBloqueantes);
        if (existeSolicitudActiva) {
            throw new IllegalArgumentException("El contenedor " + idContenedor + " ya está asociado a una solicitud activa.");
        }
        solicitud.setCliente(cliente);
        solicitud.setContenedor(contenedor);
        return solicitudRepository.save(solicitud);
    }

    public Solicitud actualizarSolicitud(Integer nroSolicitud, SolicitudDTO dto) {
        Solicitud solicitud = solicitudRepository.findById(nroSolicitud)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada con número: " + nroSolicitud));

        if (dto.getCostoEstimado() != null) {
            solicitud.setCostoEstimado(dto.getCostoEstimado());
        }
        if (dto.getTiempoEstimado() != null) {
            solicitud.setTiempoEstimado(dto.getTiempoEstimado());
        }
        if (dto.getCostoReal() != null) {
            solicitud.setCostoReal(dto.getCostoReal());
        }
        if (dto.getTiempoReal() != null) {
            solicitud.setTiempoReal(dto.getTiempoReal());
        }
        if (dto.getDniCliente() != null) {
            Cliente cliente = clienteRepository.findById(dto.getDniCliente())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con DNI: " + dto.getDniCliente()));
            solicitud.setCliente(cliente);
        }
        if (dto.getIdContenedor() != null) {
            Contenedor contenedor = contenedorRepository.findById(dto.getIdContenedor())
                    .orElseThrow(() -> new EntityNotFoundException("Contenedor no encontrado con ID: " + dto.getIdContenedor()));
            solicitud.setContenedor(contenedor);
        }
        if (dto.getIdEstado() != null) {
            Estado estado = estadoRepository.findById(dto.getIdEstado())
                    .orElseThrow(() -> new EntityNotFoundException("Estado no encontrado con ID: " + dto.getIdEstado()));
            solicitud.setEstado(estado);
        }
        return solicitudRepository.save(solicitud);
    }

    public List<Cliente> listarClientes(Integer dni) {
        if (dni != null) {
            return clienteRepository.findByDniCliente(dni);
        }
        return clienteRepository.findAll();
    }

    public Cliente crearCliente(Cliente cliente) {
        if (cliente.getDniCliente() == null) {
            throw new IllegalArgumentException("El DNI del cliente es obligatorio");
        }
        if (clienteRepository.existsByDniCliente(cliente.getDniCliente())) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el DNI " + cliente.getDniCliente());
        }
        return clienteRepository.save(cliente);
    }

    public List<Contenedor> listarContenedores() {
        return contenedorRepository.findAll();
    }

    public List<Contenedor> listarContenedoresPorEstadoNombre(String nombreEstado) {
        return contenedorRepository.findByEstado_DescripcionIgnoreCase(nombreEstado);
    }

    public List<Contenedor> listarContenedoresPorDniCliente(Integer dniCliente) {
        return contenedorRepository.findByCliente_DniCliente(dniCliente);
    }

    public List<Contenedor> listarContenedoresPorDniYEstado(Integer dniCliente, String nombreEstado) {
        return contenedorRepository.findByCliente_DniClienteAndEstado_DescripcionIgnoreCase(dniCliente, nombreEstado);
    }

    public EstadoDTO obtenerEstadoActualDeContenedor(Integer idContenedor) {
        Contenedor contenedor = contenedorRepository.findById(idContenedor)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el contenedor con id " + idContenedor));

        Estado estado = contenedor.getEstado();
        if (estado == null) {
            throw new IllegalStateException("El contenedor con id " + idContenedor + " no tiene un estado asignado");
        }
        Contexto contexto = estado.getContexto();
        if (contexto == null || contexto != Contexto.CONTENEDOR) {
            throw new IllegalStateException("El estado asociado al contenedor con id " + idContenedor + " no corresponde al contexto CONTENEDOR (contexto actual: " + contexto + ")");
        }
        EstadoDTO dto = new EstadoDTO();
        dto.setIdEstado(estado.getIdEstado());
        dto.setContexto(contexto.name());
        dto.setDescripcion(estado.getDescripcion());
        return dto;
    }

    public Contenedor crearContenedor(Contenedor contenedor) {
        if (contenedor.getPesoKg() == null || contenedor.getPesoKg() <= 0) {
            throw new IllegalArgumentException("El peso (kg) debe ser mayor que 0");
        }
        if (contenedor.getVolumenM3() == null || contenedor.getVolumenM3() <= 0) {
            throw new IllegalArgumentException("El volumen (m3) debe ser mayor que 0");
        }
        return contenedorRepository.save(contenedor);
    }

    public List<Estado> listarEstados() {
        return estadoRepository.findAll();
    }

    public Estado crearEstado(Estado estado) {
        return estadoRepository.save(estado);
    }

    public TarifaSolicitudDTO calcularTarifaSolicitud(Integer nroSolicitud) {
        Solicitud solicitud = solicitudRepository.findById(nroSolicitud)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + nroSolicitud));

        Contenedor contenedor = solicitud.getContenedor();
        if (contenedor == null) {
            throw new IllegalStateException("La solicitud no tiene contenedor asociado");
        }

        Float pesoKg = contenedor.getPesoKg();
        Float volumenM3 = contenedor.getVolumenM3();
        String origen = "OrigenDummy";
        String destino = "DestinoDummy";
        DistanciaDTO distanciaDTO = rutasClient.obtenerDistancia(origen, destino);
        double distanciaKm = distanciaDTO.getKilometros();
        String tipoContenedor = "BASE";
        double costoTraslado = flotaClient.calcularCosto(tipoContenedor, distanciaKm, pesoKg);
        String dominioCamion = "AA123BB";
        CamionFlotaDTO camion = flotaClient.obtenerCamionPorDominio(dominioCamion);

        if (camion != null) {
            if (pesoKg != null && camion.getCapacidadKg() != null && Float.compare(pesoKg, camion.getCapacidadKg()) > 0) {
                throw new IllegalArgumentException("El peso del contenedor (" + pesoKg + " kg) excede la capacidad del camión (" + camion.getCapacidadKg() + " kg)");
            }
            if (volumenM3 != null && camion.getVolumenM3() != null && volumenM3 > camion.getVolumenM3()) {
                throw new IllegalArgumentException("El volumen del contenedor (" + volumenM3 + " m3) excede el volumen máximo del camión (" + camion.getVolumenM3() + " m3)");
            }
        }
        double costoEstadia = 0.0;
        double cargosGestion = 0.0;
        double costoReal = costoTraslado + costoEstadia + cargosGestion;
        solicitud.setCostoReal((float) costoReal);
        solicitudRepository.save(solicitud);

        TarifaSolicitudDTO dto = new TarifaSolicitudDTO();
        dto.setNroSolicitud(nroSolicitud);
        dto.setDistanciaKm(distanciaKm);
        dto.setCostoTraslado(costoTraslado);
        dto.setCostoEstadia(costoEstadia);
        dto.setCargosGestion(cargosGestion);
        dto.setCostoReal(costoReal);
        return dto;
    }
}