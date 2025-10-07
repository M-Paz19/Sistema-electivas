package com.unicauca.fiet.sistema_electivas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrearElectivaDTO {
    @NotBlank
    private String codigo;

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull
    private Long departamentoId;

    @NotEmpty
    private List<Long> programasIds; // Lista de programas a los que pertenece
}
