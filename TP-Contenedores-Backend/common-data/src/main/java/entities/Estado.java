package entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "estados")
@Data
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer idEstado; // PK (int), autogenerada

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Contexto contexto;

    @Column
    private String descripcion; // Columna "descripcion" del DER

    // Relación: Un Estado puede estar en muchos ContenedTenedores
    @OneToMany(mappedBy = "estado", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Contenedor> contenedores;

    // Relación: Un Estado puede estar en muchas Solicitudes
    @OneToMany(mappedBy = "estado", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Solicitud> solicitudes;

    // Relación: Un Estado puede estar en muchos Tramos
    @OneToMany(mappedBy = "estado", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Tramo> tramos;
}