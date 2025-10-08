package com.unicauca.fiet.sistema_electivas.departamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO usado para actualizar los datos de un Departamento existente.
 *
 * Permite modificar nombre y descripción.
 */
@Getter
@Setter
public class ActualizarDepartamentoDTO {

    @NotBlank(message = "El código del departamento es obligatorio")
    @Size(max = 50, message = "El código no debe superar los 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre del departamento no puede estar vacío")
    @Size(max = 255, message = "El nombre no debe superar los 255 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
    private String descripcion;
}
