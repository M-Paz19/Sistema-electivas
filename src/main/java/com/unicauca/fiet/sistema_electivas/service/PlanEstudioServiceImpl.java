package com.unicauca.fiet.sistema_electivas.service;

import com.unicauca.fiet.sistema_electivas.dto.*;
import com.unicauca.fiet.sistema_electivas.enums.EstadoPlanEstudio;
import com.unicauca.fiet.sistema_electivas.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.model.PlanMateria;
import com.unicauca.fiet.sistema_electivas.repository.PlanEstudioRepository;
import com.unicauca.fiet.sistema_electivas.model.Programa;
import com.unicauca.fiet.sistema_electivas.repository.PlanMateriaRepository;
import com.unicauca.fiet.sistema_electivas.repository.ProgramaRepository;
import com.unicauca.fiet.sistema_electivas.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
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

    @Override
    @Transactional
    public PlanEstudioResponse crearPlan(Long programaId, PlanEstudioRequest request) {
        Programa programa = programaRepository.findById(programaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Programa con id " + programaId + " no encontrado"));

        // Validar nombre único dentro del programa
        planEstudioRepository.findByNombreAndPrograma(request.getNombre(), programa)
                .ifPresent(p -> {
                    throw new DuplicateResourceException(
                            "Ya existe un plan de estudio con el nombre '" + request.getNombre() + "' en este programa");
                });

        // Validar versión numérica
        Integer version;
        try {
            version = Integer.parseInt(request.getVersion());
        } catch (NumberFormatException e) {
            throw new BusinessException("El id del pensum debe ser un número entero (ejemplo: 544).");
        }

        // Crear plan en estado pendiente de configuración
        PlanEstudio plan = new PlanEstudio();
        plan.setNombre(request.getNombre());
        plan.setVersion(version.toString());
        plan.setEstado(EstadoPlanEstudio.CONFIGURACION_PENDIENTE);
        plan.setPrograma(programa);

        PlanEstudio saved = planEstudioRepository.save(plan);

        return new PlanEstudioResponse(
                saved.getId(),
                saved.getNombre(),
                saved.getVersion(),
                saved.getEstado().name(),
                programa.getId(),
                "Plan de estudio creado exitosamente en estado CONFIGURACION_PENDIENTE"
        );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlanEstudioListResponse> listarPlanesPorPrograma(Long programaId) {
        Programa programa = programaRepository.findById(programaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Programa con id " + programaId + " no encontrado"));

        return planEstudioRepository.findByPrograma(programa).stream()
                .map(plan -> new PlanEstudioListResponse(
                        plan.getId(),
                        plan.getNombre(),
                        plan.getVersion(),
                        plan.getEstado().getDescripcion(),
                        programa.getId(),
                        plan.getElectivasPorSemestre(),
                        plan.getReglasNivelacion(),
                        plan.getElectivasRequeridas(),
                        plan.getCreditosTotalesPlan(),
                        plan.getCreditosTrabajoGrado()
                ))
                .collect(Collectors.toList());
    }


    /**
     * Método que procesa el Excel, persiste las PlanMateria en una sola transacción
     * y actualiza el estado del plan a ACTIVO si todo sale bien.
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
                .filter(m -> "ELECTIVA".equalsIgnoreCase(m.getTipo()))
                .mapToInt(PlanMateria::getCreditos)
                .sum();

        if (creditosElectivasMaterias != creditosElectivasEsperado) {
            throw new BusinessException("La suma de créditos de las electivas no coincide. "
                    + "Encontrado: " + creditosElectivasMaterias + " vs esperado: " + creditosElectivasEsperado);
        }

        // 5. Validar créditos de trabajo de grado

        int creditosTrabajoGradoMaterias = materias.stream()
                .filter(m -> "TRABAJO_GRADO".equalsIgnoreCase(m.getTipo()))
                .mapToInt(PlanMateria::getCreditos)
                .sum();

        if (creditosTrabajoGradoMaterias != configuracion.getCreditosTrabajoGrado()) {
            throw new BusinessException("La suma de créditos del trabajo de grado no coincide. "
                    + "Encontrado: " + creditosTrabajoGradoMaterias + " vs esperado: " + configuracion.getCreditosTrabajoGrado());
        }
        // 6. Configurar plan (nuevo paso)
        configurarPlan(plan,
                configuracion.getElectivasPorSemestreJson(),
                configuracion.getReglasNivelacionJson(),
                configuracion.getElectivasRequeridas(),
                configuracion.getCreditosTrabajoGrado(),
                configuracion.getCreditosTotalesPlan());

        // 7. Persistir materias y actualizar estado
        planMateriaRepository.saveAll(materias);
        plan.setEstado(EstadoPlanEstudio.ACTIVO);
        planEstudioRepository.save(plan);


        // Si el programa estaba pendiente y ahora tiene un plan activo → aprobar
        if (programa.getEstado() == EstadoPrograma.PENDIENTE_PLAN) {
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
    private void configurarPlan(PlanEstudio plan,
                                Map<String, Object> electivasPorSemestre,
                                Map<String, Object> reglasNivelacion,
                                int electivasRequeridas,
                                int creditosTrabajoGrado,
                                int creditosTotalesPlan) {

        validarElectivasPorSemestre(electivasPorSemestre);
        validarReglasNivelacion(reglasNivelacion);

        if (electivasRequeridas <= 0) {
            throw new BusinessException("El número total de electivas debe ser mayor a 0.");
        }
        if (creditosTotalesPlan <= 0) {
            throw new BusinessException("El total de créditos del plan debe ser mayor a 0.");
        }
        if (creditosTrabajoGrado <= 0) {
            throw new BusinessException("Los créditos del trabajo de grado deben ser mayor a 0.");
        }

        plan.setElectivasPorSemestre(electivasPorSemestre);
        plan.setReglasNivelacion(reglasNivelacion);
        plan.setElectivasRequeridas(electivasRequeridas);
        plan.setCreditosTotalesPlan(creditosTotalesPlan);
        plan.setCreditosTrabajoGrado(creditosTrabajoGrado);
    }

}
