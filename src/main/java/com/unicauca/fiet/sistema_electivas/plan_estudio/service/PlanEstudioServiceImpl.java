package com.unicauca.fiet.sistema_electivas.plan_estudio.service;



import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.*;
import com.unicauca.fiet.sistema_electivas.plan_estudio.enums.EstadoPlanEstudio;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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

    /**
     * {@inheritDoc}
     */
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
        // Validar fechas de vigencia
        LocalDate vigenciaInicio = request.getVigenciaInicio();
        LocalDate vigenciaFin = request.getVigenciaFin();

        if (vigenciaInicio == null) {
            throw new BusinessException("Debe especificar la fecha de inicio de vigencia del plan de estudios.");
        }

        if (vigenciaFin != null && vigenciaFin.isBefore(vigenciaInicio)) {
            throw new BusinessException("La fecha de fin de vigencia no puede ser anterior a la fecha de inicio.");
        }

        // 🔍 Validar solapamiento solo con planes activos o en configuración
        List<PlanEstudio> planesExistentes = planEstudioRepository.findByPrograma(programa)
                .stream()
                .filter(p -> p.getEstado() == EstadoPlanEstudio.CONFIGURACION_PENDIENTE
                        || p.getEstado() == EstadoPlanEstudio.ACTIVO)
                .collect(Collectors.toList());

        for (PlanEstudio existente : planesExistentes) {
            LocalDate inicioExistente = existente.getVigenciaInicio();
            LocalDate finExistente = existente.getVigenciaFin();

            // Caso: el existente no tiene fecha fin → el administrador debe cerrarlo
            if (finExistente == null) {
                throw new BusinessException("El programa ya tiene un plan activo (" + existente.getNombre() +
                        ") sin fecha de fin. Debe cerrarlo antes de crear uno nuevo.");
            }

            // Caso: el nuevo rango se solapa con uno existente
            boolean seSolapan =
                    !(vigenciaFin != null && vigenciaFin.isBefore(inicioExistente)) && // nuevo termina antes del existente
                            !(vigenciaInicio.isAfter(finExistente)); // nuevo empieza después del existente

            if (seSolapan) {
                throw new BusinessException("El rango de vigencia ingresado se solapa con el plan existente: " +
                        existente.getNombre() + " (" + inicioExistente + " a " +
                        (finExistente != null ? finExistente : "actualidad") + ").");
            }
        }
        // Crear plan en estado pendiente de configuración
        PlanEstudio plan = new PlanEstudio();
        plan.setNombre(request.getNombre());
        plan.setVersion(version.toString());
        plan.setEstado(EstadoPlanEstudio.CONFIGURACION_PENDIENTE);
        plan.setPrograma(programa);
        plan.setVigenciaInicio(vigenciaInicio);
        plan.setVigenciaFin(vigenciaFin);

        PlanEstudio saved = planEstudioRepository.save(plan);

        return new PlanEstudioResponse(
                saved.getId(),
                saved.getNombre(),
                saved.getVersion(),
                saved.getEstado().name(),
                saved.getVigenciaInicio(),
                saved.getVigenciaFin(),
                programa.getId(),
                "Plan de estudio creado exitosamente en estado CONFIGURACION_PENDIENTE"
        );
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
                .map(plan -> new PlanEstudioListResponse(
                        plan.getId(),
                        plan.getNombre(),
                        plan.getVersion(),
                        plan.getEstado().getDescripcion(),
                        plan.getVigenciaInicio(),
                        plan.getVigenciaFin(),
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

        // Validaciones de fecha
        LocalDate vigenciaInicio = request.getVigenciaInicio();
        LocalDate vigenciaFin = request.getVigenciaFin();

        if (vigenciaFin != null && vigenciaInicio != null && vigenciaFin.isBefore(vigenciaInicio)) {
            throw new BusinessException("La fecha de fin de vigencia no puede ser anterior a la de inicio.");
        }

        switch (estadoActual) {
            case CONFIGURACION_PENDIENTE:
                // Validar nombre único dentro del programa (si cambió)
                if (!plan.getNombre().equals(request.getNombre())) {
                    planEstudioRepository.findByNombreAndPrograma(request.getNombre(), programa)
                            .ifPresent(p -> {
                                throw new DuplicateResourceException(
                                        "Ya existe un plan de estudio con el nombre '" + request.getNombre() + "' en este programa");
                            });
                    plan.setNombre(request.getNombre());
                }

                // Validar versión numérica si cambió
                if (!plan.getVersion().equals(request.getVersion())) {
                    try {
                        Integer.parseInt(request.getVersion());
                    } catch (NumberFormatException e) {
                        throw new BusinessException("La versión debe ser un número entero (ejemplo: 544).");
                    }
                    plan.setVersion(request.getVersion());
                }

                // Fechas
                if (vigenciaInicio == null) {
                    throw new BusinessException("Debe especificar la fecha de inicio de vigencia del plan.");
                }
                // 🔍 Validar solapamiento solo con planes activos o en configuración
                List<PlanEstudio> planesExistentes = planEstudioRepository.findByPrograma(programa)
                        .stream()
                        // 🔍 Excluir el mismo plan que se está actualizando
                        .filter(p -> !p.getId().equals(plan.getId()))
                        // Solo considerar los estados relevantes
                        .filter(p -> p.getEstado() == EstadoPlanEstudio.CONFIGURACION_PENDIENTE
                                || p.getEstado() == EstadoPlanEstudio.ACTIVO)
                        .collect(Collectors.toList());

                for (PlanEstudio existente : planesExistentes) {
                    LocalDate inicioExistente = existente.getVigenciaInicio();
                    LocalDate finExistente = existente.getVigenciaFin();

                    // Caso: el existente no tiene fecha fin → el administrador debe cerrarlo
                    if (finExistente == null) {
                        throw new BusinessException("El programa ya tiene un plan activo (" + existente.getNombre() +
                                ") sin fecha de fin. Debe cerrarlo antes de crear uno nuevo.");
                    }

                    // Caso: el nuevo rango se solapa con uno existente
                    boolean seSolapan =
                            !(vigenciaFin != null && vigenciaFin.isBefore(inicioExistente)) && // nuevo termina antes del existente
                                    !(vigenciaInicio.isAfter(finExistente)); // nuevo empieza después del existente

                    if (seSolapan) {
                        throw new BusinessException("El rango de vigencia ingresado se solapa con el plan existente: " +
                                existente.getNombre() + " (" + inicioExistente + " a " +
                                (finExistente != null ? finExistente : "actualidad") + ").");
                    }
                }
                plan.setVigenciaInicio(vigenciaInicio);
                plan.setVigenciaFin(vigenciaFin);
                break;

            case ACTIVO:
                // Solo se permite editar la fecha de fin
                if (vigenciaFin == null) {
                    throw new BusinessException("Debe indicar la fecha de finalización del plan activo.");
                }
                if (vigenciaFin.isBefore(plan.getVigenciaInicio())) {
                    throw new BusinessException("La fecha de fin no puede ser anterior a la de inicio del plan.");
                }
                plan.setVigenciaFin(vigenciaFin);
                break;

            default:
                throw new BusinessException("El plan en estado " + estadoActual +
                        " no puede ser modificado. Solo se permiten ediciones en CONFIGURACION_PENDIENTE o ACTIVO.");
        }

        PlanEstudio actualizado = planEstudioRepository.save(plan);

        return new PlanEstudioResponse(
                actualizado.getId(),
                actualizado.getNombre(),
                actualizado.getVersion(),
                actualizado.getEstado().name(),
                actualizado.getVigenciaInicio(),
                actualizado.getVigenciaFin(),
                programa.getId(),
                "Plan de estudio actualizado correctamente."
        );
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

        // Si está activo, validar que no sea el único activo
        if (plan.getEstado() == EstadoPlanEstudio.ACTIVO) {
            long activos = planEstudioRepository.countByProgramaAndEstado(plan.getPrograma(), EstadoPlanEstudio.ACTIVO);
            if (activos <= 1) {
                throw new InvalidStateException("No se puede desactivar el único plan activo del programa.");
            }
        }

        // Asignar vigencia fin si no tiene
        if (plan.getVigenciaFin() == null) {
            plan.setVigenciaFin(LocalDate.now());
        }

        plan.setEstado(EstadoPlanEstudio.INACTIVO);
        planEstudioRepository.save(plan);

        return new PlanEstudioResponse(
                plan.getId(),
                plan.getNombre(),
                plan.getVersion(),
                plan.getEstado().name(),
                plan.getVigenciaInicio(),
                plan.getVigenciaFin(),
                plan.getId(),
                "Plan de estudio deshabilitado correctamente."
        );

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
