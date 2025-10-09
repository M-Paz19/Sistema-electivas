package com.unicauca.fiet.sistema_electivas.periodo_academico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
/**

 DTO utilizado para crear un nuevo período académico.

 <p>Contiene la información mínima necesaria para registrar un período,

 incluyendo las fechas y el semestre correspondiente.</p>
 */
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
