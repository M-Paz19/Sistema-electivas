package com.unicauca.fiet.sistema_electivas.programa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private LocalDateTime fechaCreacion;
}
