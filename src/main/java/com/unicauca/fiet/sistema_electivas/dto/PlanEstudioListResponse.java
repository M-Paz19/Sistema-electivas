package com.unicauca.fiet.sistema_electivas.dto;


import java.util.Map;

public record PlanEstudioListResponse(
        Long id,
        String nombre,
        String version,
        String estado,
        Long programaId,
        Map<String, Object> electivasPorSemestre,
        Map<String, Object> reglasNivelacion,
        Integer electivasRequeridas,
        Integer creditosTotalesPlan,
        Integer creditosTrabajoGrado
) {}

