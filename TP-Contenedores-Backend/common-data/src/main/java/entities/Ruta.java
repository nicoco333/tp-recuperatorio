package entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * Representa una ruta logística completa, compuesta por varios tramos
 * y asociada a una solicitud específica de transporte.
 */
@Entity
@Table(name = "rutas")
@Data
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Integer idRuta; // PK autoincremental

    // 🔹 Relación con Solicitud (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nro_solicitud")
    private Solicitud solicitud; // FK hacia el microservicio de solicitudes

    // 🔹 Atributos propios de la ruta
    @Column(name = "cantidad_tramos", insertable = false)
    private Integer cantidadTramos;

    @Column(name = "cantidad_depositos", insertable = false)
    private Integer cantidadDepositos;

    // 🔹 Relación Uno a Muchos → una Ruta tiene varios Tramos
    @OneToMany(mappedBy = "ruta", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Tramo> tramos;
}
