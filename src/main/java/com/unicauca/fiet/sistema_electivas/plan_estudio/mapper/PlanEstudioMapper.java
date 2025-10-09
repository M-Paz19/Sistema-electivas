package com.unicauca.fiet.sistema_electivas.plan_estudio.mapper;


import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.ConfiguracionPlanRequest;
import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.PlanEstudioListResponse;
import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.PlanEstudioRequest;
import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.PlanEstudioResponse;
import com.unicauca.fiet.sistema_electivas.plan_estudio.enums.EstadoPlanEstudio;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;

import java.util.Objects;

/**
 * Clase utilitaria encargada de transformar objetos entre la entidad {@link PlanEstudio}
 * y los distintos DTOs utilizados en las capas de servicio y presentación.
 *
 * <p>Este mapper utiliza métodos estáticos, ya que no requiere estado ni inyección de dependencias.</p>
 */
public class PlanEstudioMapper {

    /**
     * Convierte una entidad {@link PlanEstudio} en su representación {@link PlanEstudioResponse}.
     *
     * @param plan entidad a convertir
     * @return DTO con los datos del plan de estudios
     */
    public static PlanEstudioResponse toResponse(PlanEstudio plan) {
        if (plan == null) return null;

        return new PlanEstudioResponse(
                plan.getId(),
                plan.getNombre(),
                plan.getVersion(),
                plan.getEstado().name(),
                plan.getAnioInicio(),
                plan.getPrograma() != null ? plan.getPrograma().getId() : null
        );
    }

    /**
     * Convierte una entidad {@link PlanEstudio} en su representación {@link PlanEstudioListResponse}.
     * Este DTO incluye información más detallada para listados o consultas específicas.
     *
     * @param plan entidad a convertir
     * @return DTO con información extendida
     */
    public static PlanEstudioListResponse toListResponse(PlanEstudio plan) {
        if (plan == null) return null;

        return new PlanEstudioListResponse(
                plan.getId(),
                plan.getNombre(),
                plan.getVersion(),
                plan.getEstado().name(),
                plan.getAnioInicio(),
                plan.getPrograma() != null ? plan.getPrograma().getId() : null,
                plan.getElectivasPorSemestre(),
                plan.getReglasNivelacion(),
                plan.getElectivasRequeridas(),
                plan.getCreditosTotalesPlan(),
                plan.getCreditosTrabajoGrado()
        );
    }

    /**
     * Convierte un DTO de creación {@link PlanEstudioRequest} en una entidad {@link PlanEstudio}.
     *
     * @param dto datos provenientes del cliente
     * @param programa programa académico al que pertenece el plan
     * @return entidad lista para ser persistida
     */
    public static PlanEstudio toEntity(PlanEstudioRequest dto, Programa programa) {
        PlanEstudio plan = new PlanEstudio();
        plan.setNombre(dto.getNombre());
        plan.setVersion(dto.getVersion());
        plan.setAnioInicio(dto.getAnioInicio());
        plan.setEstado(EstadoPlanEstudio.CONFIGURACION_PENDIENTE);
        plan.setPrograma(programa);
        return plan;
    }

    /**
     * Actualiza los campos configurables del plan con los datos de {@link ConfiguracionPlanRequest}.
     *
     * <p>Se usa cuando se completa la configuración de un plan ya existente.</p>
     *
     * @param plan entidad existente a actualizar
     * @param dto DTO con los nuevos valores
     */
    public static void updateFromConfiguracion(PlanEstudio plan, ConfiguracionPlanRequest dto) {
        if (dto == null) return;

        plan.setElectivasRequeridas(dto.getElectivasRequeridas());
        plan.setCreditosTrabajoGrado(dto.getCreditosTrabajoGrado());
        plan.setCreditosTotalesPlan(dto.getCreditosTotalesPlan());
        plan.setReglasNivelacion(dto.getReglasNivelacionJson());
        plan.setElectivasPorSemestre(dto.getElectivasPorSemestreJson());
    }


}
