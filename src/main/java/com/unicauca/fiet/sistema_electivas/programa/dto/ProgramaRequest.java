package com.unicauca.fiet.sistema_electivas.programa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
/**
 * DTO utilizado para la creación de un {@code Programa}.
 * <p>
 * Contiene los datos mínimos requeridos (código y nombre)
 * que deben ser enviados por el cliente.
 */
@Getter
@Setter
public class ProgramaRequest {
    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
}
