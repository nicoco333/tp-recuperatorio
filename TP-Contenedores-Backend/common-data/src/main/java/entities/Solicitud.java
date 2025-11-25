package entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "solicitudes")
@Data
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nro_solicitud")
    private Integer nroSolicitud; // PK (int), autogenerada

    @Column(name = "costo_estimado")
    private Float costoEstimado; // Tipo "float" en el DER

    @Column(name = "tiempo_estimado")
    private Integer tiempoEstimado; // Tipo "int" en el DER

    @Column(name = "costo_real")
    private Float costoReal; // Tipo "float" en el DER

    @Column(name = "tiempo_real")
    private Integer tiempoReal; // Tipo "int" en el DER

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp // hace que Hibernate ponga la fecha/hora antes del INSERT
    private LocalDateTime fechaCreacion;

    // Relación "realiza" (inversa): Muchas Solicitudes son de un Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni_cliente", nullable = false) // FK (int)
    private Cliente cliente;

    // Relación "asociado_a" (inversa): Muchas Solicitudes para un Contenedor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contenedor", nullable = false) // FK (int)
    private Contenedor contenedor;

    // Relación "estado" (inversa): Muchas Solicitudes tienen un Estado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false) // FK (int)
    private Estado estado;

    // Relación "genera": Una Solicitud puede tener muchas Rutas
    @OneToMany(mappedBy = "solicitud", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Ruta> rutas;
}