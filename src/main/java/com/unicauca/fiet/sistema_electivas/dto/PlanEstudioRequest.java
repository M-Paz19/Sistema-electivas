package com.unicauca.fiet.sistema_electivas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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

    private Map<String, Object> reglasNivelacion;


    private Map<String, Object> electivasPorSemestre;


    private Integer electivasRequeridas;


    private Integer creditosTotalesPlan;


    private Integer creditosTrabajoGrado;
}
