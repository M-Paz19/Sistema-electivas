package com.unicauca.fiet.sistema_electivas.plan_estudio.service;


import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.PlanMateriaResponse;

import java.util.List;

/**
 * Servicio para la gestión de materias dentro de un plan de estudio.
 * 
 * <p>Define las operaciones de lectura relacionadas con las materias
 * que pertenecen a un determinado plan.</p>
 */
public interface PlanMateriaService {

    /**
     * Obtiene todas las materias asociadas a un plan de estudio.
     *
     * @param planId ID del plan de estudio.
     * @return Lista de {@link PlanMateriaResponse} con la información de las materias.
     * @throws ResourceNotFoundException si el plan no existe.
     */
    List<PlanMateriaResponse> listarMateriasPorPlan(Long planId);
}
