package com.tpi.backend.geolocalizacion.service;

import com.tpi.backend.geolocalizacion.client.SolicitudClient;
import com.tpi.backend.geolocalizacion.client.TarifaClient;
import com.tpi.backend.geolocalizacion.dto.DistanciaTotalRutaDTO;
import com.tpi.backend.geolocalizacion.dto.TarifaDTO;
import com.tpi.backend.geolocalizacion.dto.distanciageo.DistanciaDTO;
import com.tpi.backend.geolocalizacion.repository.*;
import entities.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
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
    private final SolicitudClient solicitudClient;

    public RutaService(RutaRepository rutaRepository, GeolocalizacionRepository geolocalizacionRepository,
                       TipoTramoRepository tipoTramoRepository, EstadoRepository estadoRepository,
                       CamionRepository camionRepository, TramoRepository tramoRepository,
                       DepositoRepository depositoRepository, GeoService geoService,
                       TarifaClient tarifaClient, SolicitudClient solicitudClient) {

        this.rutaRepository = rutaRepository;
        this.geolocalizacionRepository = geolocalizacionRepository;
        this.tipoTramoRepository = tipoTramoRepository;
        this.estadoRepository = estadoRepository;
        this.camionRepository = camionRepository;
        this.tramoRepository = tramoRepository;
        this.depositoRepository = depositoRepository;
        this.geoService = geoService;
        this.tarifaClient = tarifaClient;
        this.solicitudClient = solicitudClient;
    }

    // ... (listarRutas, crearRuta, listarTramos sin cambios) ...
    public List<Ruta> listarRutas(Integer idRuta) {
        if (idRuta != null) return rutaRepository.findById(idRuta).map(List::of).orElseGet(List::of);
        return rutaRepository.findAll();
    }

    public Ruta crearRuta(Ruta ruta) {
        if (ruta.getSolicitud().getNroSolicitud() == null) throw new IllegalArgumentException("nroSolicitud obligatorio.");
        return rutaRepository.save(ruta);
    }

    public List<Tramo> listarTramosPorRuta(Integer idRuta) {
        if (!rutaRepository.existsById(idRuta)) throw new EntityNotFoundException("Ruta no encontrada: " + idRuta);
        return tramoRepository.findByRuta_IdRuta(idRuta);
    }

    // --- MODIFICADO: CREAR TRAMO (ENVÍA ESTIMADOS) ---
    public Tramo crearTramo(Tramo tramo) {
        // ... (Validaciones de ruta, tipo, estado, camión - IGUAL QUE ANTES) ...
        if (tramo.getRuta() == null || tramo.getRuta().getIdRuta() == null) throw new IllegalArgumentException("idRuta obligatorio.");
        Ruta ruta = rutaRepository.findById(tramo.getRuta().getIdRuta()).orElseThrow();
        tramo.setRuta(ruta);
        
        tramo.setOrigenGeo(resolverOrigen(tramo));
        tramo.setDestinoGeo(resolverDestino(tramo));
        
        TipoTramo tipo = tipoTramoRepository.findById(tramo.getTipoTramo().getIdTipoTramo()).orElseThrow();
        tramo.setTipoTramo(tipo);
        
        Estado estado = estadoRepository.findById(3).orElseThrow(); // PENDIENTE
        tramo.setEstado(estado);
        
        if (tramo.getCamion() != null) {
            Camion c = camionRepository.findById(tramo.getCamion().getDominioCamion()).orElseThrow();
            tramo.setCamion(c);
        }

        // Cálculos
        DistanciaDTO dist = calcularDistanciaEntre(tramo.getOrigenGeo(), tramo.getDestinoGeo());
        
        LocalDateTime inicio = tramo.getFechaHoraInicioEstimada() != null ? tramo.getFechaHoraInicioEstimada() : LocalDateTime.now();
        tramo.setFechaHoraInicioEstimada(inicio);
        
        if (dist != null && dist.getDuracionMinutos() > 0) {
            tramo.setFechaHoraFinEstimada(inicio.plusMinutes(dist.getDuracionMinutos()));
        }

        Float costoAproximado = calcularCostoAproximado(tramo.getCamion(), dist);
        tramo.setCostoAproximado(costoAproximado);

        tramo.setIdTramo(null);
        Tramo guardado = tramoRepository.save(tramo);

        // ** NOTIFICAR ESTIMADOS A LA SOLICITUD **
        Integer idSol = guardado.getRuta().getSolicitud().getNroSolicitud();
        if (idSol != null) {
            Double cEst = (costoAproximado != null) ? costoAproximado.doubleValue() : 0.0;
            Integer tEst = (dist != null) ? (int) dist.getDuracionMinutos() : 0;
            
            // Enviamos cEst, tEst. Reales y estado van null.
            solicitudClient.notificarProgreso(idSol, cEst, tEst, null, null, null);
        }

        return guardado;
    }

    // ... (asignarCamionATramo, etc. sin cambios mayores) ...
    public Tramo asignarCamionATramo(Integer idTramo, String dominioCamion) {
        Tramo tramo = tramoRepository.findById(idTramo).orElseThrow();
        Camion camion = camionRepository.findById(dominioCamion).orElseThrow();
        if (!camion.getDisponibilidad()) throw new IllegalStateException("Camión no disponible");
        tramo.setCamion(camion);
        // Recalcular costo si ya tiene geografía
        if (tramo.getOrigenGeo() != null && tramo.getDestinoGeo() != null) {
            DistanciaDTO dist = calcularDistanciaEntre(tramo.getOrigenGeo(), tramo.getDestinoGeo());
            tramo.setCostoAproximado(calcularCostoAproximado(camion, dist));
        }
        return tramoRepository.save(tramo);
    }

    // --- MODIFICADO: INICIO TRAMO (SOLO ESTADO) ---
    public Tramo registrarInicioTramo(Integer idTramo, LocalDateTime fechaHoraInicioReal) {
        Tramo tramo = tramoRepository.findById(idTramo).orElseThrow();
        
        LocalDateTime inicioReal = (fechaHoraInicioReal != null) ? fechaHoraInicioReal : LocalDateTime.now();
        tramo.setFechaHoraInicioReal(inicioReal);
        
        // Estado: EN CURSO (ID 7)
        Estado enCurso = estadoRepository.findById(7).orElseThrow();
        tramo.setEstado(enCurso);

        // Si es primer tramo, solicitud -> EN CURSO
        if (tramo.getOrden() != null && tramo.getOrden() == 1) {
            Integer idSol = tramo.getRuta().getSolicitud().getNroSolicitud();
            if (idSol != null) {
                solicitudClient.notificarProgreso(idSol, null, null, null, null, "EN CURSO");
            }
        }
        return tramoRepository.save(tramo);
    }

    // --- MODIFICADO: FIN TRAMO (ENVÍA REALES) ---
    public Tramo registrarFinTramo(Integer idTramo, LocalDateTime fechaHoraFinReal) {
        Tramo tramo = tramoRepository.findById(idTramo).orElseThrow();
        
        LocalDateTime finReal = (fechaHoraFinReal != null) ? fechaHoraFinReal : LocalDateTime.now();
        tramo.setFechaHoraFinReal(finReal);
        
        // Estado: FINALIZADO (ID 8)
        Estado finalizado = estadoRepository.findById(8).orElseThrow();
        tramo.setEstado(finalizado);

        // Costo Real
        if (tramo.getCostoReal() == null) {
            tramo.setCostoReal(tramo.getCostoAproximado());
        }
        
        // ** NOTIFICAR REALES A LA SOLICITUD **
        Integer idSol = tramo.getRuta().getSolicitud().getNroSolicitud();
        if (idSol != null) {
            Double costoTramo = (tramo.getCostoReal() != null) ? tramo.getCostoReal().doubleValue() : 0.0;
            
            // Calcular Tiempo Real
            Integer tiempoTramo = 0;
            if (tramo.getFechaHoraInicioReal() != null && tramo.getFechaHoraFinReal() != null) {
                long minutos = Duration.between(tramo.getFechaHoraInicioReal(), tramo.getFechaHoraFinReal()).toMinutes();
                tiempoTramo = (int) minutos;
            }

            // Verificar si cerramos la solicitud
            String nuevoEstado = null;
            Integer cantTotal = tramo.getRuta().getCantidadTramos();
            if (cantTotal != null && tramo.getOrden() != null && tramo.getOrden().equals(cantTotal)) {
                nuevoEstado = "FINALIZADA";
            }
            
            solicitudClient.notificarProgreso(idSol, null, null, costoTramo, tiempoTramo, nuevoEstado);
        }

        return tramoRepository.save(tramo);
    }

    // ... (rest of helper methods: obtenerDistancia, resolverOrigen, etc. mantén los que ya tenías) ...
    public DistanciaDTO obtenerDistanciaDeTramo(Integer idTramo) {
        Tramo t = tramoRepository.findById(idTramo).orElseThrow();
        return calcularDistanciaEntre(resolverOrigen(t), resolverDestino(t));
    }
    
    // ... helpers privados (resolverOrigen, resolverDestino, calcularCostoAproximado, obtenerCostoLitro, calcularDistanciaEntre) IGUAL QUE ANTES ...
    
    // NOTA: Asegúrate de incluir aquí los métodos privados 'resolverOrigen', 'resolverDestino', 
    // 'calcularDistanciaEntre', 'calcularCostoAproximado' y 'obtenerCostoLitroCombustible' 
    // tal como estaban en tu versión anterior. No han cambiado de lógica interna.
    
    private Geolocalizacion resolverOrigen(Tramo tramo) {
        if (tramo.getOrigenDeposito() != null && tramo.getOrigenDeposito().getIdDeposito() != null) {
            Long idDep = tramo.getOrigenDeposito().getIdDeposito();
            Deposito deposito = depositoRepository.findById(idDep).orElseThrow();
            tramo.setOrigenDeposito(deposito);
            return deposito.getGeolocalizacion();
        }
        if (tramo.getOrigenGeo() != null) {
             return geolocalizacionRepository.findById(tramo.getOrigenGeo().getIdGeo()).orElseThrow();
        }
        throw new IllegalArgumentException("Origen requerido");
    }

    private Geolocalizacion resolverDestino(Tramo tramo) {
        if (tramo.getDestinoDeposito() != null && tramo.getDestinoDeposito().getIdDeposito() != null) {
            Long idDep = tramo.getDestinoDeposito().getIdDeposito();
            Deposito deposito = depositoRepository.findById(idDep).orElseThrow();
            tramo.setDestinoDeposito(deposito);
            return deposito.getGeolocalizacion();
        }
        if (tramo.getDestinoGeo() != null) {
             return geolocalizacionRepository.findById(tramo.getDestinoGeo().getIdGeo()).orElseThrow();
        }
        throw new IllegalArgumentException("Destino requerido");
    }

    private DistanciaDTO calcularDistanciaEntre(Geolocalizacion origen, Geolocalizacion destino) {
        String o = origen.getLongitud() + "," + origen.getLatitud();
        String d = destino.getLongitud() + "," + destino.getLatitud();
        try { return geoService.calcularDistancia(o, d); } catch(Exception e) { throw new RuntimeException(e); }
    }

    private Float calcularCostoAproximado(Camion camion, DistanciaDTO dist) {
        if(camion==null || dist==null) return null;
        Float costoLitro = obtenerCostoLitroCombustible(camion);
        return (float) (dist.getKilometros() * camion.getConsumoPromKm() * costoLitro);
    }

    private Float obtenerCostoLitroCombustible(Camion camion) {
        TarifaDTO t = tarifaClient.obtenerTarifaPorCamion(camion.getDominioCamion());
        return t != null ? t.getCostoLitroCombustible() : 0f;
    }
    
    public DistanciaTotalRutaDTO obtenerDistanciaTotalRuta(Integer idRuta) { return new DistanciaTotalRutaDTO(); } // Placeholder simple
    public List<Deposito> listarDepositos() { return depositoRepository.findAll(); }
    public Deposito crearDeposito(Deposito d) { return depositoRepository.save(d); }
    public DistanciaDTO obtenerDistanciaPorSolicitud(Integer nro) { return new DistanciaDTO(); } // Placeholder
}