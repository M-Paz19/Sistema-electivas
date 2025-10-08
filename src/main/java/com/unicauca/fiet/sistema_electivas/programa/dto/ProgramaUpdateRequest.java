package com.unicauca.fiet.sistema_electivas.programa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO utilizado para actualizar un programa existente.
 * <p>
 * Contiene únicamente el campo que puede ser modificado: el nombre del programa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramaUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

}

