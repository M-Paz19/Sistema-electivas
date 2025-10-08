package com.unicauca.fiet.sistema_electivas.programa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para la operación de deshabilitar un programa académico.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProgramaDisableResponse {
    private Long id;
    private String nombre;
    private String mensaje;
}

