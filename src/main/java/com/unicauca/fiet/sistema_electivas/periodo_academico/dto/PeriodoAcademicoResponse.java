package com.unicauca.fiet.sistema_electivas.periodo_academico.dto;


import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**

 DTO de salida que representa la información resumida de un {@link PeriodoAcademico}.

 <p>Se usa en las respuestas de consulta o listados de períodos académicos.</p>

 */
@Getter
@Setter
@AllArgsConstructor
public class PeriodoAcademicoResponse {

    private Long id;
    private String semestre;
    private Instant fechaApertura;
    private Instant fechaCierre;
    private String estado;
}
