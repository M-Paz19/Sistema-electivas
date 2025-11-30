package com.unicauca.fiet.sistema_electivas.asignacion.repository;


import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import com.unicauca.fiet.sistema_electivas.asignacion.model.AsignacionElectiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionElectivaRepository extends JpaRepository<AsignacionElectiva, Long> {

    /**
     * Obtiene asignaciones filtradas por la oferta y por una lista de estados.
     *
     * @param ofertaId ID de la oferta académica.
     * @param estados lista de estados válidos de asignación.
     * @return lista de asignaciones que coinciden con los filtros.
     */
    List<AsignacionElectiva> findByOfertaIdAndEstadoAsignacionIn(
            Long ofertaId,
            List<EstadoAsignacion> estados
    );

    /**
     * Obtiene todas las asignaciones de un estudiante dentro de un período académico.
     *
     * @param codigoEstudiante código único del estudiante.
     * @param periodoId ID del período académico.
     * @return lista de asignaciones del estudiante para dicho período.
     */
    @Query("""
        SELECT a
        FROM AsignacionElectiva a
        WHERE a.estudianteCodigo = :codigoEstudiante
          AND a.oferta.periodo.id = :periodoId
    """)
    List<AsignacionElectiva> findByEstudianteAndPeriodo(
            @Param("codigoEstudiante") String codigoEstudiante,
            @Param("periodoId") Long periodoId
    );

    /**
     * Obtiene todas las asignaciones realizadas en un período académico.
     *
     * @param periodoId ID del período.
     * @return lista completa de asignaciones asociadas al período.
     */
    @Query("""
        SELECT a
        FROM AsignacionElectiva a
        JOIN a.oferta o
        WHERE o.periodo.id = :periodoId
    """)
    List<AsignacionElectiva> findByPeriodoId(Long periodoId);

    /**
     * Recupera el historial completo de asignaciones de un estudiante,
     * ordenado desde el semestre más reciente al más antiguo.
     *
     * @param codigo código del estudiante.
     * @return lista ordenada de asignaciones históricas.
     */
    @Query("""
    SELECT ae
    FROM AsignacionElectiva ae
    JOIN ae.oferta o
    JOIN o.periodo p
    WHERE ae.estudianteCodigo = :codigo
    ORDER BY p.semestre DESC
    """)
    List<AsignacionElectiva> findHistorialAsignaciones(String codigo);

}