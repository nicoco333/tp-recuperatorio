package entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "tipo_tramo")
@Data
public class TipoTramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_tramo")
    private Integer idTipoTramo; // PK (int), autogenerada

    @Column(name = "nombre_tipo", nullable = false)
    private String nombreTipo;

    @OneToMany(mappedBy = "tipoTramo", fetch = FetchType.LAZY)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<Tramo> tramos;

}
