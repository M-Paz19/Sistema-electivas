package com.unicauca.fiet.sistema_electivas.plan_estudio.controller;

import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.PlanEstudioListResponse;
import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.PlanMateriaResponse;
import com.unicauca.fiet.sistema_electivas.plan_estudio.enums.EstadoPlanEstudio;
import com.unicauca.fiet.sistema_electivas.plan_estudio.service.PlanEstudioService;
import com.unicauca.fiet.sistema_electivas.plan_estudio.service.PlanMateriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planes")
@RequiredArgsConstructor
public class PlanEstudioGlobalController {

    private final PlanEstudioService planEstudioService;
    private final PlanMateriaService planMateriaService;

    /**
     * Lista todos los planes de estudio del sistema, con opción de filtrar por estado.
     *
     * @param estado (opcional) Estado por el cual filtrar.
     * @return Lista de planes.
     */
    @GetMapping
    public ResponseEntity<List<PlanEstudioListResponse>> listarTodosLosPlanes(
            @RequestParam(required = false) EstadoPlanEstudio estado) {

        List<PlanEstudioListResponse> response = planEstudioService.listarTodosLosPlanes(estado);
        return ResponseEntity.ok(response);
    }
    /**
     * Obtiene la lista de materias pertenecientes a un plan de estudios específico.
     *
     * <p>Este endpoint retorna todas las materias asociadas al plan identificado por el parámetro
     * {@code planId}. Cada materia se representa mediante un {@link PlanMateriaResponse}, el cual
     * contiene información básica como nombre, semestre, tipo y créditos.</p>
     *
     * @param planId identificador del plan de estudios del cual se desean consultar las materias
     * @return una respuesta HTTP 200 (OK) con la lista de materias del plan
     */
    @GetMapping("/{planId}")
    public ResponseEntity<List<PlanMateriaResponse>> listarMateriasPorPlan(
            @PathVariable Long planId) {

        List<PlanMateriaResponse> response = planMateriaService.listarMateriasPorPlan(planId);
        return ResponseEntity.ok(response);
    }
}
