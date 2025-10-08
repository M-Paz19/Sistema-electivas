package com.unicauca.fiet.sistema_electivas.plan_estudio.dto;


import java.time.LocalDate;
import java.util.Map;
/**
 * DTO utilizado para devolver la información de una lista de {@code PlanEstudio}.
 */
public record PlanEstudioListResponse(
        Long id,
        String nombre,
        String version,
        String estado,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFin,
        Long programaId,
        Map<String, Object> electivasPorSemestre,
        Map<String, Object> reglasNivelacion,
        Integer electivasRequeridas,
        Integer creditosTotalesPlan,
        Integer creditosTrabajoGrado
) {}

