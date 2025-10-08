package com.unicauca.fiet.sistema_electivas.plan_estudio.controller;


import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.*;
import com.unicauca.fiet.sistema_electivas.plan_estudio.enums.EstadoPlanEstudio;
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.plan_estudio.service.PlanEstudioService;
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
     * Lista los planes de estudio de un programa, con opción de filtrar por estado.
     *
     * <p>Si no se especifica el estado, se devuelven todos los planes asociados al programa.</p>
     *
     * @param programaId ID del programa.
     * @param estado     (opcional) Estado por el cual filtrar: ACTIVO, INACTIVO, CONFIGURACION_PENDIENTE.
     * @return Lista de {@link PlanEstudioListResponse} con los planes encontrados.
     */
    @GetMapping
    public ResponseEntity<List<PlanEstudioListResponse>> listarPlanesPorPrograma(
            @PathVariable Long programaId,
            @RequestParam(required = false) EstadoPlanEstudio estado) {

        List<PlanEstudioListResponse> response = planEstudioService.listarPlanesPorPrograma(programaId, estado);
        return ResponseEntity.ok(response);
    }


    /**
     * Actualiza la información de un plan de estudio existente.
     *
     * <p>Reglas:</p>
     * <ul>
     *   <li>Si el plan está en estado {@code CONFIGURACION_PENDIENTE}, se pueden modificar
     *       los campos {@code nombre}, {@code versión}, {@code vigenciaInicio} y {@code vigenciaFin}.</li>
     *   <li>Si el plan está en estado {@code ACTIVO}, solo se permite modificar {@code vigenciaFin}.</li>
     *   <li>No se permite cambiar el programa al que pertenece el plan.</li>
     * </ul>
     *
     * @param programaId ID del programa al que pertenece el plan.
     * @param planId     ID del plan a actualizar.
     * @param request    Datos actualizados del plan.
     * @return {@link PlanEstudioResponse} con la información del plan actualizado.
     * @throws BusinessException si se intenta modificar un campo no permitido o las fechas son inválidas.
     * @throws ResourceNotFoundException si el plan o el programa no existen.
     */
    @PutMapping("/{planId}")
    public ResponseEntity<PlanEstudioResponse> actualizarPlan(
            @PathVariable Long programaId,
            @PathVariable Long planId,
            @Valid @RequestBody PlanEstudioRequest request) {

        PlanEstudioResponse response = planEstudioService.actualizarPlan(programaId, planId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Desactiva un plan de estudio.
     *
     * <p>Reglas:</p>
     * <ul>
     *   <li>Solo se pueden desactivar los planes en estado {@code ACTIVO} o {@code CONFIGURACION_PENDIENTE}.</li>
     *   <li>Si el plan está {@code ACTIVO}, no debe ser el único plan activo del programa.</li>
     *   <li>Si el plan no tiene fecha de finalización, se asignará la fecha actual.</li>
     * </ul>
     *
     * @param programaId ID del programa al que pertenece el plan.
     * @param planId     ID del plan a desactivar.
     * @return {@link PlanEstudioResponse} con la información del plan desactivado.
     * @throws InvalidStateException si el plan no puede ser desactivado por su estado actual o por ser el único activo.
     * @throws ResourceNotFoundException si el plan no existe.
     */
    @PatchMapping("/{planId}/desactivar")
    public ResponseEntity<PlanEstudioResponse> desactivarPlan(
            @PathVariable Long programaId,
            @PathVariable Long planId) {

        PlanEstudioResponse response = planEstudioService.desactivarPlan(planId);
        return ResponseEntity.ok(response);
    }

    /**
     * Carga la malla curricular de un plan de estudio y configura sus reglas.
     *
     * <p>Esta operación activa automáticamente el plan si la carga es exitosa.</p>
     *
     * <p>Validaciones:</p>
     * <ul>
     *   <li>El archivo de malla debe tener el formato válido (por ejemplo, Excel).</li>
     *   <li>Debe enviarse junto con la configuración del plan (reglas de nivelación, electivas, créditos, etc.).</li>
     *   <li>El plan debe estar en estado {@code CONFIGURACION_PENDIENTE} para poder cargar la malla.</li>
     * </ul>
     *
     * @param programaId    ID del programa asociado.
     * @param planId        ID del plan de estudio al que se cargará la malla.
     * @param file          Archivo de malla curricular (formato Excel).
     * @param configuracion Objeto JSON con las configuraciones del plan (electivas, créditos, reglas, etc.).
     * @param bindingResult Resultado de validación del objeto {@code configuracion}.
     * @return {@link MallaUploadResponse} con el resumen del proceso de carga.
     * @throws BusinessException si hay errores en la validación o conflicto con el estado del plan.
     */
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

