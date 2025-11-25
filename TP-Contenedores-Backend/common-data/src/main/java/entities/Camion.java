package entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entidad que representa un camión dentro de la flota.
 * Cada camión puede estar asignado a un transportista y tener una tarifa base asociada.
 */
@Entity
@Table(name = "camiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {

    @Id
    @Column(nullable = false, name = "dominio_camion")
    private String dominioCamion; // PK (String), no es @GeneratedValue

    @Column(nullable = false, name = "capacidad_peso_max")
    private Float capacidadPesoMax;

    @Column(nullable = false, name = "capacidad_volumen_max")
    private Float capacidadVolumenMax;

    @Column
    private Boolean disponibilidad;

    @Column(nullable = false, name = "consumo_prom_km")
    private Float consumoPromKm;

    @Column(nullable = false, name = "costo_traslado")
    private Float costoTraslado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transportista")
    private Transportista transportista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_geo")
    private Geolocalizacion idGeo;

    // Relación "aplica": Un Camion puede tener muchas Tarifas
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Tarifa> tarifas;

    // Relación "asigna": Un Camion puede ser asignado a muchos Tramo
    @OneToMany(mappedBy = "camion", fetch = FetchType.LAZY)
    private List<Tramo> tramos;

}
