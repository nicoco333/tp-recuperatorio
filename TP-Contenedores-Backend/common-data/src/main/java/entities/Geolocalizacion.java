package entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "geolocalizacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Geolocalizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "id_geo")
    private Integer idGeo; // PK autoincremental

    @Column
    private String direccion;

    @Column(nullable = false)
    private Float latitud;

    @Column(nullable = false)
    private Float longitud;
}

