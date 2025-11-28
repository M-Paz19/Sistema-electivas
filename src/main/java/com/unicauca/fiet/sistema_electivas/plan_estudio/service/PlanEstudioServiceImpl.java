package com.unicauca.fiet.sistema_electivas.plan_estudio.service;



import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.*;
import com.unicauca.fiet.sistema_electivas.plan_estudio.enums.EstadoPlanEstudio;
import com.unicauca.fiet.sistema_electivas.plan_estudio.mapper.PlanEstudioMapper;
import com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanMateria;
import com.unicauca.fiet.sistema_electivas.plan_estudio.repository.PlanEstudioRepository;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import com.unicauca.fiet.sistema_electivas.plan_estudio.repository.PlanMateriaRepository;
import com.unicauca.fiet.sistema_electivas.programa.repository.ProgramaRepository;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de planes de estudio.
 */
@Service
@RequiredArgsConstructor
public class PlanEstudioServiceImpl implements PlanEstudioService {

    private final PlanEstudioRepository planEstudioRepository;
    private final ProgramaRepository programaRepository;
    private final ExcelParserService excelParserService;
    private final PlanMateriaRepository planMateriaRepository;
    private final PeriodoAcademicoRepository periodoAcademicoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PlanEstudioResponse crearPlan(Long programaId, PlanEstudioRequest request) {
        Programa programa = programaRepository.findById(programaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Programa con id " + programaId + " no encontrado"));

        // Validar que el estado del Programa no sea DESHABILITADO
        if (programa.getEstado() == EstadoPrograma.DESHABILITADO) {
            throw new BusinessException("No se puede crear un un plan para un programa con estado DESHABILITADO. "
                    + programa.getNombre() +" con estado actual: " + programa.getEstado());
        }
        // Validar nombre único dentro del programa
        planEstudioRepository.findByNombreAndPrograma(request.getNombre(), programa)
                .ifPresent(p -> {
                    throw new DuplicateResourceException(
                            "Ya existe un plan de estudio con el nombre '" + request.getNombre() + "' en este programa");
                });

        // Validar versión numérica
        try {
            Integer.parseInt(request.getVersion());
        } catch (NumberFormatException e) {
            throw new BusinessException("La version del plan debe ser un número entero (ejemplo: 544).");
        }
        // Validar año de inicio
        Integer anio = request.getAnioInicio();
        if (anio == null || anio < 1900 || anio > 3000) {
            throw new BusinessException("Debe especificar un año de inicio válido (entre 1900 y 3000).");
        }
        // Validar que no haya otro plan en el mismo año
        if (planEstudioRepository.existsByProgramaAndAnioInicio(programa, request.getAnioInicio())) {
            throw new BusinessException(
                    "Ya existe un plan de estudio para el año " + request.getAnioInicio() + " en este programa."
            );
        }

        // Crear entidad desde el DTO usando el mapper
        PlanEstudio plan = PlanEstudioMapper.toEntity(request, programa);
        PlanEstudio saved = planEstudioRepository.save(plan);

        // Retornar respuesta con mensaje
        return PlanEstudioMapper.toResponse(saved);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlanEstudioListResponse> listarPlanesPorPrograma(Long programaId, @Nullable EstadoPlanEstudio estado) {
        Programa programa = programaRepository.findById(programaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Programa con id " + programaId + " no encontrado"));

        List<PlanEstudio> planes = (estado == null)
                ? planEstudioRepository.findByPrograma(programa)
                : planEstudioRepository.findByProgramaAndEstado(programa, estado);

        return planes.stream()
                .map(PlanEstudioMapper::toListResponse) // uso directo del mapper
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlanEstudioListResponse> listarTodosLosPlanes(@Nullable EstadoPlanEstudio estado) {

        List<PlanEstudio> planes = (estado == null)
                ? planEstudioRepository.findAll()
                : planEstudioRepository.findByEstado(estado);

        return planes.stream()
                .map(PlanEstudioMapper::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PlanEstudioResponse actualizarPlan(Long programaId, Long planId, PlanEstudioRequest request) {
        Programa programa = programaRepository.findById(programaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Programa con id " + programaId + " no encontrado"));

        PlanEstudio plan = planEstudioRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan de estudio con id " + planId + " no encontrado"));

        // Validar que pertenezca al programa
        if (!plan.getPrograma().getId().equals(programa.getId())) {
            throw new BusinessException("El plan indicado no pertenece al programa especificado.");
        }

        EstadoPlanEstudio estadoActual = plan.getEstado();

        //  Validar año de inicio
        Integer nuevoAnio = request.getAnioInicio();
        if (nuevoAnio == null) {
            throw new BusinessException("Debe especificar el año de inicio del plan de estudios.");
        }

        if (nuevoAnio < 1900 || nuevoAnio > 3000) {
            throw new BusinessException("El año de inicio debe estar entre 1900 y 3000.");
        }

        switch (estadoActual) {
            case CONFIGURACION_PENDIENTE:
                //  Validar nombre único dentro del programa (si cambió)
                if (!plan.getNombre().equals(request.getNombre())) {
                    planEstudioRepository.findByNombreAndPrograma(request.getNombre(), programa)
                            .ifPresent(p -> {
                                throw new DuplicateResourceException(
                                        "Ya existe un plan de estudio con el nombre '" + request.getNombre() + "' en este programa");
                            });
                    plan.setNombre(request.getNombre());
                }

                //  Validar versión numérica (si cambió)
                if (!plan.getVersion().equals(request.getVersion())) {
                    try {
                        Integer.parseInt(request.getVersion());
                    } catch (NumberFormatException e) {
                        throw new BusinessException("La versión debe ser un número entero (ejemplo: 544).");
                    }
                    plan.setVersion(request.getVersion());
                }

                // Validar que no exista otro plan del mismo año
                boolean existePlanMismoAnio = planEstudioRepository
                        .existsByProgramaAndAnioInicio(programa, nuevoAnio);

                if (existePlanMismoAnio && !Objects.equals(plan.getAnioInicio(), nuevoAnio)) {
                    throw new BusinessException("Ya existe un plan de estudio en este programa con el año " + nuevoAnio + ".");
                }

                // Actualizar año
                plan.setAnioInicio(nuevoAnio);
                break;

            case ACTIVO:
                // No se permite cambiar nombre, versión ni año en un plan activo
                throw new BusinessException("No se permite editar un plan activo. Solo se pueden cerrar o desactivar.");

            default:
                throw new BusinessException("El plan en estado " + estadoActual +
                        " no puede ser modificado. Solo se permiten ediciones en CONFIGURACION_PENDIENTE.");
        }

        PlanEstudio actualizado = planEstudioRepository.save(plan);

        // Usamos el mapper para crear la respuesta

        return PlanEstudioMapper.toResponse(actualizado);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PlanEstudioResponse desactivarPlan(Long planId) {
        PlanEstudio plan = planEstudioRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de estudio no encontrado."));

        if (plan.getEstado() != EstadoPlanEstudio.ACTIVO &&
                plan.getEstado() != EstadoPlanEstudio.CONFIGURACION_PENDIENTE) {
            throw new InvalidStateException("Solo los planes activos o en configuración pueden desactivarse.");
        }

        // 2. Validar que no haya un período académico activo
        if (periodoAcademicoRepository.existsByEstadoIn(EstadoPeriodoAcademico.obtenerEstadosActivos()) && plan.getEstado() == EstadoPlanEstudio.ACTIVO) {
            throw new InvalidStateException("No se puede deshabilitar un plan activo cuando hay un periodo académico ACTIVO en cualquiera de sus etapas.");
        }
        // Si está activo, validar que no sea el único activo
        if (plan.getEstado() == EstadoPlanEstudio.ACTIVO) {
            long activos = planEstudioRepository.countByProgramaAndEstado(plan.getPrograma(), EstadoPlanEstudio.ACTIVO);
            if (activos <= 1) {
                throw new InvalidStateException("No se puede desactivar el único plan activo del programa.");
            }
        }

        // Asignar vigencia fin si no tiene
        plan.setEstado(EstadoPlanEstudio.INACTIVO);

        planEstudioRepository.save(plan);

        //  Usar el mapper para generar la respuesta
        return PlanEstudioMapper.toResponse(plan);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MallaUploadResponse cargarMallaCurricular(
            Long programaId,
            Long planId,
            MultipartFile file,
            ConfiguracionPlanRequest configuracion
    ) {
        // 1. Verificar existencia y pertenencia del programa y plan
        Programa programa = programaRepository.findById(programaId)
                .orElseThrow(() -> new ResourceNotFoundException("Programa con id " + programaId + " no encontrado"));

        PlanEstudio plan = planEstudioRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de estudio con id " + planId + " no encontrado"));

        // Comprobar que el plan pertenece al programa
        if (plan.getPrograma() == null || !plan.getPrograma().getId().equals(programa.getId())) {
            throw new BusinessException("El plan indicado no pertenece al programa especificado.");
        }
        // Validar estado del plan antes de permitir la carga de malla
        if (plan.getEstado() != EstadoPlanEstudio.CONFIGURACION_PENDIENTE) {
            throw new BusinessException("Solo se puede cargar la malla en planes con estado CONFIGURACION_PENDIENTE. "
                    + "Estado actual: " + plan.getEstado());
        }
        // Validar que el estado del Programa no sea DESHABILITADO
        if (programa.getEstado() == EstadoPrograma.DESHABILITADO) {
            throw new BusinessException("Solo se puede cargar la malla en planes cuyo programa no este DESHABILITADO. "
                    + programa.getNombre() +" con estado actual: " + programa.getEstado());
        }

        // Validar que no haya un período académico activo
        if (periodoAcademicoRepository.existsByEstadoIn(EstadoPeriodoAcademico.obtenerEstadosActivos())) {
            throw new InvalidStateException("No se puede activar un plan cuando hay un periodo académico ACTIVO en cualquiera de sus etapas.");
        }

        // 2. Parsear el archivo a entidades PlanMateria (no guardadas aún)
        List<PlanMateria> materias = excelParserService.parsearMaterias(file, plan);

        // 3. Validar suma de créditos: total
        int sumaCreditosMaterias = materias.stream()
                .mapToInt(PlanMateria::getCreditos)
                .sum();



        if (sumaCreditosMaterias != configuracion.getCreditosTotalesPlan()) {
            throw new BusinessException("Revise los créditos del plan, no coinciden con los ingresados manualmente. "
                    + "Suma encontrada: " + sumaCreditosMaterias + " vs esperado: " + configuracion.getCreditosTotalesPlan());
        }

        // 4. Validar créditos de electivas
        int creditosElectivasEsperado = 3 * configuracion.getElectivasRequeridas();
        int creditosElectivasMaterias = materias.stream()
                .filter(m -> "ELECTIVA".equalsIgnoreCase(m.getTipo().name()))
                .mapToInt(PlanMateria::getCreditos)
                .sum();

        if (creditosElectivasMaterias != creditosElectivasEsperado) {
            throw new BusinessException("La suma de créditos de las electivas no coincide. "
                    + "Encontrado: " + creditosElectivasMaterias + " vs esperado: " + creditosElectivasEsperado);
        }

        // 5. Validar créditos de trabajo de grado
        int creditosTrabajoGradoMaterias = materias.stream()
                .filter(m -> "TRABAJO_GRADO".equalsIgnoreCase(m.getTipo().name()))
                .mapToInt(PlanMateria::getCreditos)
                .sum();

        if (creditosTrabajoGradoMaterias != configuracion.getCreditosTrabajoGrado()) {
            throw new BusinessException("La suma de créditos del trabajo de grado no coincide. "
                    + "Encontrado: " + creditosTrabajoGradoMaterias + " vs esperado: " + configuracion.getCreditosTrabajoGrado());
        }
        // 6. Configurar plan (nuevo paso)

        validarElectivasPorSemestre(configuracion.getElectivasPorSemestreJson());
        validarReglasNivelacion(configuracion.getReglasNivelacionJson());
        // Usa el mapper para actualizar el plan desde el DTO
        PlanEstudioMapper.updateFromConfiguracion(plan, configuracion);
        // 7. Persistir materias y actualizar estado
        planMateriaRepository.saveAll(materias);
        plan.setEstado(EstadoPlanEstudio.ACTIVO);
        planEstudioRepository.save(plan);


        // Si el programa estaba pendiente y ahora tiene un plan activo → aprobar
        if (programa.getEstado() == EstadoPrograma.BORRADOR) {
            programa.setEstado(EstadoPrograma.APROBADO);
            programaRepository.save(programa);
        }


        return new MallaUploadResponse(materias.size(),
                "Malla curricular cargada correctamente. Se procesaron " + materias.size() + " materias.");
    }

    /**
     * Válida que las reglas de nivelación tengan el formato esperado.
     * Formato esperado:
     * {
     *   "Octavo": { "minCreditosAprobados": 112, "maxPeriodosMatriculados": 7 },
     *   "Noveno": { "minCreditosAprobados": 132, "maxPeriodosMatriculados": 8 }
     * }
     */
    private void validarReglasNivelacion(Map<String, Object> reglasNivelacion) {
        if (reglasNivelacion == null || reglasNivelacion.isEmpty()) {
            throw new BusinessException("Las reglas de nivelación no pueden estar vacías.");
        }

        for (var entry : reglasNivelacion.entrySet()) {
            String semestre = entry.getKey();
            Object value = entry.getValue();

            if (!(value instanceof Map)) {
                throw new BusinessException("Formato inválido en '" + semestre
                        + "'. Debe ser un objeto con claves: minCreditosAprobados, maxPeriodosMatriculados.");
            }

            Map<?, ?> regla = (Map<?, ?>) value;

            // Validar presencia de claves obligatorias
            if (!regla.containsKey("minCreditosAprobados") || !regla.containsKey("maxPeriodosMatriculados")) {
                throw new BusinessException("La regla de nivelación para '" + semestre
                        + "' debe incluir 'minCreditosAprobados' y 'maxPeriodosMatriculados'.");
            }

            // Validar que ambos sean enteros positivos
            Object minCreditos = regla.get("minCreditosAprobados");
            Object maxPeriodos = regla.get("maxPeriodosMatriculados");

            if (!(minCreditos instanceof Integer) || !(maxPeriodos instanceof Integer)) {
                throw new BusinessException("En el semestre '" + semestre
                        + "', 'minCreditosAprobados' y 'maxPeriodosMatriculados' deben ser números enteros.");
            }

            if ((Integer) minCreditos <= 0 || (Integer) maxPeriodos <= 0) {
                throw new BusinessException("En el semestre '" + semestre
                        + "', los valores deben ser mayores a 0.");
            }
        }
    }

    /**
     * Valida que las electivas por semestre tengan formato { "semestre": numero }.
     */
    private void validarElectivasPorSemestre(Map<String, Object> electivasPorSemestre) {
        if (electivasPorSemestre == null || electivasPorSemestre.isEmpty()) {
            throw new BusinessException("Las electivas por semestre no pueden estar vacías.");
        }

        for (var entry : electivasPorSemestre.entrySet()) {
            String semestre = entry.getKey();
            Object value = entry.getValue();

            if (!(value instanceof Integer)) {
                throw new BusinessException("El valor de electivas en semestre '" + semestre
                        + "' debe ser un número entero.");
            }

            if ((Integer) value < 0) {
                throw new BusinessException("El número de electivas en semestre '" + semestre
                        + "' no puede ser negativo.");
            }
        }
    }


}
