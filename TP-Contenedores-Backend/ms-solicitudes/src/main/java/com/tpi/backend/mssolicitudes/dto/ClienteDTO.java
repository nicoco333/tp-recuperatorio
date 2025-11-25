package com.tpi.backend.mssolicitudes.dto;

import lombok.Data;

/**
 * DTO para representar clientes.
 */
@Data
public class ClienteDTO {
    private Integer dniCliente;
    private String nombre;
    private String apellido;
    private String telefono;
}
