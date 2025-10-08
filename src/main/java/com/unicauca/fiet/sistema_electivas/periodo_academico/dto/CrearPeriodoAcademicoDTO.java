package com.unicauca.fiet.sistema_electivas.periodo_academico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
public class CrearPeriodoAcademicoDTO {
    @NotBlank(message = "El semestre es obligatorio")
    private String semestre;

    @NotNull(message = "La fecha de apertura es obligatoria")
    private Instant fechaApertura;

    @NotNull(message = "La fecha de cierre es obligatoria")
    private Instant fechaCierre;

}
