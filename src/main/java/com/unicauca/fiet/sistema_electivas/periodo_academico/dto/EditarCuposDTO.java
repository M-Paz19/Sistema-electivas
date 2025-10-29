package com.unicauca.fiet.sistema_electivas.periodo_academico.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class EditarCuposDTO {
    @NotNull(message = "Debe especificar los cupos por programa")
    private Map<Long, Integer> cuposPorPrograma;
}
