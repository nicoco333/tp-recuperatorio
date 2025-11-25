package entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "contenedores")
@Data
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenedor")
    private Integer idContenedor; // PK (int), autogenerada

    @Column(name = "peso_kg", nullable = false)
    private Float pesoKg; // Tipo "float" en el DER

    @Column(name = "volumen_m3", nullable = false)
    private Float volumenM3; // Tipo "float" en el DER

    @Column(name = "costo_base_km")
    private Float costoBaseKm; // Tipo "float" en el DER

    // Relación "posee" (inversa): Muchos Contenedores pertenecen a un Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false) // FK (int)
    private Cliente cliente;

    // Relación "estado" (inversa): Muchos Contenedores tienen un Estado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false) // FK (int)
    private Estado estado;

    // Relación "asociado_a": Un Contenedor puede estar en muchas Solicitudes
    @OneToMany(mappedBy = "contenedor", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Solicitud> solicitudes;
}