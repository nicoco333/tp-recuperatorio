package entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tramos")
@Data
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tramo")
    private Integer idTramo;

    // Relación con Ruta (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ruta", nullable = false)
    private Ruta ruta;

    // Relaciones geográficas (origen y destino)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origen_geo")
    private Geolocalizacion origenGeo;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_geo")
    private Geolocalizacion destinoGeo;

    // Depósitos asociados

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origen_deposito_id")
    private Deposito origenDeposito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_deposito_id")
    private Deposito destinoDeposito;

    // Tipo de tramo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_tramo", nullable = false)
    private TipoTramo tipoTramo; // FK al catálogo de tipos de tramo (tabla referencial)

    // Estado actual del tramo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    // Fechas previstas
    @Column(name = "fechahora_inicio_estimada")
    private LocalDateTime fechaHoraInicioEstimada;

    @Column(name = "fechahora_fin_estimada")
    private LocalDateTime fechaHoraFinEstimada;

    // Fechas reales
    @Column(name = "fechahora_inicio_real")
    private LocalDateTime fechaHoraInicioReal;

    @Column(name = "fechahora_fin_real")
    private LocalDateTime fechaHoraFinReal;

    // Costos
    @Column(name = "costo_aproximado")
    private Float costoAproximado;

    @Column(name = "costo_real")
    private Float costoReal;

    // Relación lógica con microservicio Flota
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dominio_camion")
    private Camion camion;
}
