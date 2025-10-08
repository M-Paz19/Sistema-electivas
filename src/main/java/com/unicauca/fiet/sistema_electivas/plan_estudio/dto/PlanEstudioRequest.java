package com.unicauca.fiet.sistema_electivas.plan_estudio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO utilizado para la creación de un {@code PlanEstudio}.
 *
 * Contiene los datos mínimos requeridos para registrar un plan
 * dentro de un programa académico.
 */
@Getter
@Setter
public class PlanEstudioRequest {

    @NotBlank(message = "El nombre del plan es obligatorio")
    private String nombre;

    @NotBlank(message = "La versión es obligatoria")
    private String version;

    @NotNull(message = "La fecha de inicio de vigencia del plan es obligatoria")
    private LocalDate vigenciaInicio;

    private LocalDate vigenciaFin;

    private Map<String, Object> reglasNivelacion;


    private Map<String, Object> electivasPorSemestre;


    private Integer electivasRequeridas;


    private Integer creditosTotalesPlan;


    private Integer creditosTrabajoGrado;
}
