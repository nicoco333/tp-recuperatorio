package com.tpi.backend.geolocalizacion.service;

import com.tpi.backend.msrutas.client.TarifaClient;
import com.tpi.backend.msrutas.dto.DistanciaTotalRutaDTO;
import com.tpi.backend.msrutas.dto.TarifaDTO;
import com.tpi.backend.msrutas.dto.geolocalizacion.DistanciaDTO;
import com.tpi.backend.msrutas.repository.*;
import entities.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RutaService {

    private final RutaRepository rutaRepository;
    private final GeolocalizacionRepository geolocalizacionRepository;
    private final TipoTramoRepository tipoTramoRepository;
    private final EstadoRepository estadoRepository;
    private final CamionRepository camionRepository;
    private final TramoRepository tramoRepository;
    private final DepositoRepository depositoRepository;
    private final GeoService geoService;
    private final TarifaClient tarifaClient;

    public RutaService(RutaRepository rutaRepository, GeolocalizacionRepository geolocalizacionRepository, TipoTramoRepository tipoTramoRepository, EstadoRepository estadoRepository, CamionRepository camionRepository,
                       TramoRepository tramoRepository, DepositoRepository depositoRepository, GeoService geoService, TarifaClient tarifaClient) {

        this.rutaRepository = rutaRepository;
        this.geolocalizacionRepository = geolocalizacionRepository;
        this.tipoTramoRepository = tipoTramoRepository;
        this.estadoRepository = estadoRepository;
        this.camionRepository = camionRepository;
        this.tramoRepository = tramoRepository;
        this.depositoRepository = depositoRepository;
        this.geoService = geoService;
        this.tarifaClient = tarifaClient;
    }

    // -------- RUTAS --------
    public List<Ruta> listarRutas(Integer idRuta) {
        if (idRuta != null) {
            return rutaRepository.findById(idRuta)
                    .map(List::of)
                    .orElseGet(List::of);
        } else {
            return rutaRepository.findAll();
        }
    }

    public Ruta crearRuta(Ruta ruta) {
        if (ruta.getSolicitud().getNroSolicitud() == null) {
            throw new IllegalArgumentException("El número de solicitud (nroSolicitud) es obligatorio para crear una ruta.");
        }
        return rutaRepository.save(ruta);
    }

    public List<Tramo> listarTramosPorRuta(Integer idRuta) {
        if (!rutaRepository.existsById(idRuta)) {
            throw new EntityNotFoundException("No se encontró la ruta con ID: " + idRuta);
        }
        return tramoRepository.findByRuta_IdRuta(idRuta);
    }

    public Tramo crearTramo(Tramo tramo) {
        // ---------- RUTA OBLIGATORIA ----------
        if (tramo.getRuta() == null || tramo.getRuta().getIdRuta() == null) {
            throw new IllegalArgumentException("idRuta es obligatorio.");
        }

        Ruta ruta = rutaRepository.findById(tramo.getRuta().getIdRuta())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe ruta con id " + tramo.getRuta().getIdRuta()
                ));
        tramo.setRuta(ruta);

        // ---------- ORIGEN / DESTINO GEO  ----------
        Geolocalizacion origenGeo = resolverOrigen(tramo);
        Geolocalizacion destinoGeo = resolverDestino(tramo);

        tramo.setOrigenGeo(origenGeo);
        tramo.setDestinoGeo(destinoGeo);

        // ---------- TIPO DE TRAMO ----------
        if (tramo.getTipoTramo() == null || tramo.getTipoTramo().getIdTipoTramo() == null) {
            throw new IllegalArgumentException("tipoTramo es obligatorio.");
        }

        TipoTramo tipoTramo = tipoTramoRepository.findById(tramo.getTipoTramo().getIdTipoTramo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe tipo de tramo con id " + tramo.getTipoTramo().getIdTipoTramo()
                ));
        tramo.setTipoTramo(tipoTramo);

        // ---------- ESTADO ----------
        Estado estado;
        if (tramo.getEstado() != null && tramo.getEstado().getIdEstado() != null) {
            estado = estadoRepository.findById(tramo.getEstado().getIdEstado())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe estado con id " + tramo.getEstado().getIdEstado()
                    ));
        } else {
            estado = estadoRepository.findByDescripcion("PENDIENTE")
                    .orElseThrow(() -> new IllegalStateException(
                            "No se encontró estado con descripcion 'PENDIENTE'"
                    ));
        }
        tramo.setEstado(estado);

        // ---------- CAMIÓN ----------
        if (tramo.getCamion() != null && tramo.getCamion().getDominioCamion() != null) {
            Camion camion = camionRepository.findById(tramo.getCamion().getDominioCamion())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe camión con dominio " + tramo.getCamion().getDominioCamion()
                    ));
            tramo.setCamion(camion);
        }

        // ---------- GEO + COSTO + TIEMPOS----------
        DistanciaDTO dist = calcularDistanciaEntre(origenGeo, destinoGeo);

        // 1) fechaHoraInicioEstimada
        LocalDateTime inicioEstimada = tramo.getFechaHoraInicioEstimada();
        if (inicioEstimada == null) {
            inicioEstimada = LocalDateTime.now();
        }
        tramo.setFechaHoraInicioEstimada(inicioEstimada);

        // 2) fechaHoraFinEstimada
        if (dist != null && dist.getDuracionMinutos() > 0) {
            tramo.setFechaHoraFinEstimada(inicioEstimada.plusMinutes(dist.getDuracionMinutos()));
        }

        // 3) costo aproximado
        Float costoAproximado = calcularCostoAproximado(tramo.getCamion(), dist);
        tramo.setCostoAproximado(costoAproximado);

        tramo.setIdTramo(null);
        return tramoRepository.save(tramo);
    }

    // ================== HELPERS PRIVADOS ==================

    private Float calcularCostoAproximado(Camion camion, DistanciaDTO dist) {
        if (camion == null || dist == null) {
            return null;
        }
        double kms = dist.getKilometros();
        Float consumoPromKm = camion.getConsumoPromKm();
        Float costoLitroCombustible = obtenerCostoLitroCombustible(camion);

        if (kms <= 0 || consumoPromKm == null || consumoPromKm <= 0
                || costoLitroCombustible == null || costoLitroCombustible <= 0) {
            return null;
        }
        double litrosTotales = kms * consumoPromKm;
        double costo = litrosTotales * costoLitroCombustible;
        return (float) costo;
    }

    private Float obtenerCostoLitroCombustible(Camion camion) {
        if (camion == null || camion.getDominioCamion() == null) {
            return null;
        }
        TarifaDTO tarifa = tarifaClient.obtenerTarifaPorCamion(camion.getDominioCamion());
        if (tarifa == null || tarifa.getCostoLitroCombustible() == null) {
            throw new IllegalStateException("No se encontró tarifa con costoLitroCombustible para el camión " + camion.getDominioCamion());
        }
        return tarifa.getCostoLitroCombustible();
    }

    private Geolocalizacion resolverOrigen(Tramo tramo) {
        if (tramo.getOrigenDeposito() != null && tramo.getOrigenDeposito().getIdDeposito() != null) {
            Long idDep = tramo.getOrigenDeposito().getIdDeposito();
            Deposito deposito = depositoRepository.findById(idDep)
                    .orElseThrow(() -> new IllegalArgumentException("No existe depósito origen con id " + idDep));
            tramo.setOrigenDeposito(deposito);
            if (deposito.getGeolocalizacion() == null) {
                throw new IllegalStateException("El depósito origen " + idDep + " no tiene geolocalización asociada");
            }
            return deposito.getGeolocalizacion();
        }
        if (tramo.getOrigenGeo() != null && tramo.getOrigenGeo().getIdGeo() != null) {
            Integer idGeo = tramo.getOrigenGeo().getIdGeo();
            Geolocalizacion geo = geolocalizacionRepository.findById(idGeo)
                    .orElseThrow(() -> new IllegalArgumentException("No existe geolocalización origen con id " + idGeo));
            tramo.setOrigenGeo(geo);
            return geo;
        }
        throw new IllegalArgumentException("Debe indicar origenGeo o origenDepositoId para el tramo.");
    }

    private Geolocalizacion resolverDestino(Tramo tramo) {
        if (tramo.getDestinoDeposito() != null && tramo.getDestinoDeposito().getIdDeposito() != null) {
            Long idDep = tramo.getDestinoDeposito().getIdDeposito();
            Deposito deposito = depositoRepository.findById(idDep)
                    .orElseThrow(() -> new IllegalArgumentException("No existe depósito destino con id " + idDep));
            tramo.setDestinoDeposito(deposito);
            if (deposito.getGeolocalizacion() == null) {
                throw new IllegalStateException("El depósito destino " + idDep + " no tiene geolocalización asociada");
            }
            return deposito.getGeolocalizacion();
        }
        if (tramo.getDestinoGeo() != null && tramo.getDestinoGeo().getIdGeo() != null) {
            Integer idGeo = tramo.getDestinoGeo().getIdGeo();
            Geolocalizacion geo = geolocalizacionRepository.findById(idGeo)
                    .orElseThrow(() -> new IllegalArgumentException("No existe geolocalización destino con id " + idGeo));
            tramo.setDestinoGeo(geo);
            return geo;
        }
        throw new IllegalArgumentException("Debe indicar destinoGeo o destinoDepositoId para el tramo.");
    }

    // --- MODIFICADO PARA OSRM ---
    private DistanciaDTO calcularDistanciaEntre(Geolocalizacion origen, Geolocalizacion destino) {
        // IMPORTANTE: OSRM espera "Longitud,Latitud", NO "Latitud,Longitud"
        String origenStr = origen.getLongitud() + "," + origen.getLatitud();
        String destinoStr = destino.getLongitud() + "," + destino.getLatitud();

        try {
            return geoService.calcularDistancia(origenStr, destinoStr);
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular distancia entre origen y destino", e);
        }
    }

    public Tramo asignarCamionATramo(Integer idTramo, String dominioCamion) {
        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new IllegalArgumentException("No existe tramo con id " + idTramo));

        if (dominioCamion == null || dominioCamion.isBlank()) {
            throw new IllegalArgumentException("El dominioCamion es obligatorio.");
        }
        Camion camion = camionRepository.findById(dominioCamion)
                .orElseThrow(() -> new IllegalArgumentException("No existe camión con dominio " + dominioCamion));

        if (Boolean.FALSE.equals(camion.getDisponibilidad())) {
            throw new IllegalStateException("El camión " + dominioCamion + " no está disponible.");
        }
        tramo.setCamion(camion);

        if (tramo.getOrigenGeo() != null && tramo.getDestinoGeo() != null) {
            DistanciaDTO dist = calcularDistanciaEntre(tramo.getOrigenGeo(), tramo.getDestinoGeo());
            Float costoAproximado = calcularCostoAproximado(camion, dist);
            tramo.setCostoAproximado(costoAproximado);
        }
        return tramoRepository.save(tramo);
    }

    public Tramo registrarInicioTramo(Integer idTramo, LocalDateTime fechaHoraInicioReal) {
        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new IllegalArgumentException("No existe tramo con id " + idTramo));
        LocalDateTime inicioReal = (fechaHoraInicioReal != null) ? fechaHoraInicioReal : LocalDateTime.now();
        tramo.setFechaHoraInicioReal(inicioReal);
        return tramoRepository.save(tramo);
    }

    public Tramo registrarFinTramo(Integer idTramo, LocalDateTime fechaHoraFinReal) {
        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new IllegalArgumentException("No existe tramo con id " + idTramo));
        LocalDateTime finReal = (fechaHoraFinReal != null) ? fechaHoraFinReal : LocalDateTime.now();
        if (tramo.getFechaHoraInicioReal() != null && finReal.isBefore(tramo.getFechaHoraInicioReal())) {
            throw new IllegalArgumentException("La fechaHoraFinReal no puede ser anterior a fechaHoraInicioReal.");
        }
        tramo.setFechaHoraFinReal(finReal);
        return tramoRepository.save(tramo);
    }

    public DistanciaDTO obtenerDistanciaDeTramo(Integer idTramo) {
        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new IllegalArgumentException("No existe tramo con id " + idTramo));
        Geolocalizacion origen = resolverOrigen(tramo);
        Geolocalizacion destino = resolverDestino(tramo);
        return calcularDistanciaEntre(origen, destino);
    }

    public DistanciaTotalRutaDTO obtenerDistanciaTotalRuta(Integer idRuta) {
        Ruta ruta = rutaRepository.findById(idRuta)
                .orElseThrow(() -> new IllegalArgumentException("No existe ruta con id " + idRuta));
        List<Tramo> tramos = tramoRepository.findByRuta_IdRuta(idRuta);

        if (tramos.isEmpty()) {
            DistanciaTotalRutaDTO dto = new DistanciaTotalRutaDTO();
            dto.setIdRuta(idRuta);
            dto.setCantidadTramos(0);
            dto.setDistanciaTotalKm(0.0);
            dto.setDuracionTotalMinutos(0L);
            return dto;
        }

        double totalKm = 0.0;
        long totalMin = 0L;

        for (Tramo tramo : tramos) {
            Geolocalizacion origen = resolverOrigen(tramo);
            Geolocalizacion destino = resolverDestino(tramo);
            DistanciaDTO dist = calcularDistanciaEntre(origen, destino);
            totalKm += dist.getKilometros();
            totalMin += dist.getDuracionMinutos();
        }

        DistanciaTotalRutaDTO dto = new DistanciaTotalRutaDTO();
        dto.setIdRuta(idRuta);
        dto.setCantidadTramos(tramos.size());
        dto.setDistanciaTotalKm(totalKm);
        dto.setDuracionTotalMinutos(totalMin);
        return dto;
    }

    public List<Deposito> listarDepositos() {
        return depositoRepository.findAll();
    }

    public Deposito crearDeposito(Deposito deposito) {
        if (deposito.getNombre() == null || deposito.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del depósito es obligatorio.");
        }
        if (deposito.getGeolocalizacion() != null && deposito.getGeolocalizacion().getIdGeo() == null) {
            geolocalizacionRepository.save(deposito.getGeolocalizacion());
        }
        return depositoRepository.save(deposito);
    }
}