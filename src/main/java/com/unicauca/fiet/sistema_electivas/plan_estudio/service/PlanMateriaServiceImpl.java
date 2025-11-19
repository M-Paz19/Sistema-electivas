package com.unicauca.fiet.sistema_electivas.plan_estudio.service;

import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.PlanMateriaResponse;
import com.unicauca.fiet.sistema_electivas.plan_estudio.mapper.PlanMateriaMapper;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanMateria;
import com.unicauca.fiet.sistema_electivas.plan_estudio.repository.PlanEstudioRepository;
import com.unicauca.fiet.sistema_electivas.plan_estudio.repository.PlanMateriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio {@link PlanMateriaService}.
 *
 * <p>Se encarga de obtener las materias que pertenecen a un
 * plan de estudio específico. Valida que el plan exista antes
 * de recuperar sus materias.</p>
 */
@Service
@RequiredArgsConstructor
public class PlanMateriaServiceImpl implements PlanMateriaService {

    private final PlanEstudioRepository planEstudioRepository;
    private final PlanMateriaRepository planMateriaRepository;

    /**
     * {@inheritDoc}
     *
     * <p>El método es de solo lectura y utiliza transacciones
     * declarativas para asegurar consistencia sin bloquear
     * operaciones de escritura.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlanMateriaResponse> listarMateriasPorPlan(Long planId) {

        // Validar que el plan exista
        PlanEstudio plan = planEstudioRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan de estudio con id " + planId + " no encontrado"));

        // Obtener materias asociadas
        List<PlanMateria> materias = planMateriaRepository.findByPlanEstudios(plan);

        // Convertir entidades a DTOs
        return materias.stream()
                .map(PlanMateriaMapper::toResponse)
                .collect(Collectors.toList());
    }
}