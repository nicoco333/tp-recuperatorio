package com.tpi.backend.msflota.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TransportistaDTO {
    private Integer idTransportista;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;
    private LocalDate fechaNacimiento;
    private boolean activo;
}