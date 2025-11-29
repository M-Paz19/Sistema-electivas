package com.unicauca.fiet.sistema_electivas.periodo_academico.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoRespuestaFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RespuestasFormularioRepository extends JpaRepository<RespuestasFormulario, Long> {

    @EntityGraph(attributePaths = {
            "programa", "periodo", "opciones", "opciones.oferta", "opciones.oferta.electiva"
    })
    List<RespuestasFormulario> findByPeriodoId(Long periodoId);

    List<RespuestasFormulario> findByEstado(EstadoRespuestaFormulario estadoRespuestaFormulario);

    List<RespuestasFormulario> findByPeriodoIdAndEstado(Long periodoId, EstadoRespuestaFormulario estado);

    @Query("""
        SELECT r.codigoEstudiante
        FROM RespuestasFormulario r
        WHERE r.periodo.id = :periodoId
        AND r.estado IN :estados
        """)
    List<String> findCodigosByPeriodoAndEstados(
            @Param("periodoId") Long periodoId,
            @Param("estados") List<EstadoRespuestaFormulario> estados
    );

    long countByPeriodoIdAndEstadoIn(Long periodoId, List<EstadoRespuestaFormulario> estados);

    List<RespuestasFormulario> findByPeriodoIdAndEstadoIn(Long periodoId, List<EstadoRespuestaFormulario> estados);

    // Validaciones de duplicados
    boolean existsByPeriodoIdAndCodigoEstudianteAndEstado(
            Long periodoId, String codigoEstudiante, EstadoRespuestaFormulario estado
    );

    // MÉTODO NUEVO: Valida si existe el código en otro registro distinto al actual (para edición)
    boolean existsByPeriodoIdAndCodigoEstudianteAndIdNot(
            Long periodoId, String codigoEstudiante, Long id
    );

    // Validar duplicados exactos al importar
    boolean existsByPeriodoAndCodigoEstudianteAndTimestampRespuesta(
            PeriodoAcademico periodo, String codigoEstudiante, Instant timestampRespuesta
    );

    boolean existsByPeriodoIdAndEstadoIn(Long periodoId, List<EstadoRespuestaFormulario> estados);
}