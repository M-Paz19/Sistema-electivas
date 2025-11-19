package com.unicauca.fiet.sistema_electivas.plan_estudio.mapper;

import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.PlanMateriaResponse;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanMateria;

import java.util.List;

/**
 * Clase utilitaria encargada de transformar entidades {@link PlanMateria}
 * a sus correspondientes DTOs utilizados en la capa de servicio y presentación.
 *
 * <p>Todos los métodos son estáticos, ya que no requieren mantener estado.</p>
 */
public class PlanMateriaMapper {

    /**
     * Convierte una entidad {@link PlanMateria} en un {@link PlanMateriaResponse}.
     *
     * @param entidad entidad PlanMateria a convertir
     * @return DTO con la información necesaria del plan-materia
     */
    public static PlanMateriaResponse toResponse(PlanMateria entidad) {
        if (entidad == null) return null;

        return new PlanMateriaResponse(
                entidad.getId(),
                entidad.getPlanEstudios() != null ? entidad.getPlanEstudios().getId() : null,
                entidad.getNombre(),
                entidad.getSemestre(),
                entidad.getTipo() != null ? entidad.getTipo().name() : null,
                entidad.getCreditos()
        );
    }

    /**
     * Convierte una lista de entidades {@link PlanMateria} en una lista de {@link PlanMateriaResponse}.
     *
     * @param lista lista de entidades a convertir
     * @return lista de DTOs
     */
    public static List<PlanMateriaResponse> toResponseList(List<PlanMateria> lista) {
        if (lista == null) return List.of();

        return lista.stream()
                .map(PlanMateriaMapper::toResponse)
                .toList();
    }
}
