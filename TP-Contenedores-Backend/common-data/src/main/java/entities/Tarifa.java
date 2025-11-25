package entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa una tarifa aplicable a un traslado.
 * Puede depender del tipo de contenedor, la distancia y el peso total.
 */
@Entity
@Table(name = "tarifas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa")
    private Integer idTarifa;

    @Column(nullable = false, name = "tipo_tarifa")
    private String tipoTarifa;

    @Column(name = "costo_litro_combustible")
    private Float costoLitroCombustible;

    @Column(name = "cargo_gestion_tramo")
    private Float cargoGestionTramo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dominio_camion")
    private Camion camion;

    public Object getCostoHora() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCostoHora'");
    }

}
