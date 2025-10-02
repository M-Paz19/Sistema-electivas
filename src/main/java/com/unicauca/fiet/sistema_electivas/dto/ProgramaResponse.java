package com.unicauca.fiet.sistema_electivas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de respuesta para operaciones relacionadas con {@code Programa}.
 * <p>
 * Se utiliza para devolver la información del programa al cliente
 * después de crear, editar o consultar un programa.
 */
@Getter
@Setter
@AllArgsConstructor
public class ProgramaResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String estado;
    private String mensaje;
}
