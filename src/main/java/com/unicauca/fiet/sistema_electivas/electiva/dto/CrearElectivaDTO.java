package com.unicauca.fiet.sistema_electivas.electiva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
/**
 * DTO usado para la creación de una nueva Electiva.
 *
 * Contiene solo los campos requeridos para registrar el recurso.
 */
@Getter
@Setter
public class CrearElectivaDTO {
    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El ID del departamento es obligatorio")
    private Long departamentoId;

    @NotEmpty(message = "La electiva debe ofertarse en al menos un programa")
    private List<Long> programasIds; // Lista de programas a los que pertenece
}
