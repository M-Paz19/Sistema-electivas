package com.unicauca.fiet.sistema_electivas.controller;


import com.unicauca.fiet.sistema_electivas.dto.*;
import com.unicauca.fiet.sistema_electivas.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.service.PlanEstudioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para la gestión de Planes de Estudio.
 * Expone los endpoints para crear y listar planes por programa.
 */
@RestController
@RequestMapping("/api/programas/{programaId}/planes")
@RequiredArgsConstructor
public class PlanEstudioController {

    private final PlanEstudioService planEstudioService;

    /**
     * Crea un nuevo plan de estudio asociado a un programa.
     *
     * @param programaId ID del programa al que pertenece el plan.
     * @param request    Datos del plan a crear.
     * @return PlanEstudioResponse con la información del plan creado.
     */
    @PostMapping
    public ResponseEntity<PlanEstudioResponse> crearPlan(
            @PathVariable Long programaId,
            @Valid @RequestBody PlanEstudioRequest request) {
        PlanEstudioResponse response = planEstudioService.crearPlan(programaId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos los planes de estudio asociados a un programa.
     *
     * @param programaId ID del programa.
     * @return Lista de PlanEstudioResponse con los planes asociados.
     */
    @GetMapping
    public ResponseEntity<List<PlanEstudioListResponse>> listarPlanesPorPrograma(
            @PathVariable Long programaId) {
        return ResponseEntity.ok(planEstudioService.listarPlanesPorPrograma(programaId));
    }

    @PostMapping(value = "/{planId}/malla", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MallaUploadResponse> cargarMalla(
            @PathVariable Long programaId,
            @PathVariable Long planId,
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("configuracion") ConfiguracionPlanRequest configuracion,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            // Devuelve el primer error encontrado
            throw new BusinessException(bindingResult.getFieldError().getDefaultMessage());
        }

        MallaUploadResponse response = planEstudioService.cargarMallaCurricular(
                programaId, planId, file, configuracion);

        return ResponseEntity.status(201).body(response);
    }

}

