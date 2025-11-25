package entities;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Representa un depósito intermedio o final dentro de una ruta.
 */
@Entity
@Table(name = "depositos")
@Data
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_deposito")
    private Long idDeposito;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, name = "costo_estadia_diaria")
    private Float costoEstadiaDiaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_geo")
    private Geolocalizacion geolocalizacion;

}
