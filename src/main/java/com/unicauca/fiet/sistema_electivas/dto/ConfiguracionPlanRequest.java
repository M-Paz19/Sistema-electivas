package com.unicauca.fiet.sistema_electivas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * DTO utilizado para recibir una peticion de la configuracion de {@code PlanEstudio} junto con la malla curricular.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguracionPlanRequest {

    @NotNull(message = "El número de electivas requeridas es obligatorio")
    @Min(value = 1, message = "El número de electivas debe ser mayor a 0")
    private Integer electivasRequeridas;

    @NotNull(message = "Los créditos del trabajo de grado son obligatorios")
    @Min(value = 1, message = "Los créditos del trabajo de grado deben ser mayores a 0")
    private Integer creditosTrabajoGrado;

    @NotNull(message = "El total de créditos del plan es obligatorio")
    @Min(value = 1, message = "El total de créditos debe ser mayor a 0")
    private Integer creditosTotalesPlan;

    @NotNull(message = "Las reglas de nivelación son obligatorias")
    @NotEmpty(message = "Las reglas de nivelación no pueden estar vacías")
    private Map<String, Object> reglasNivelacionJson;

    @NotNull(message = "Las electivas por semestre son obligatorias")
    @NotEmpty(message = "Las electivas por semestre no pueden estar vacías")
    private Map<String, Object> electivasPorSemestreJson;
}
