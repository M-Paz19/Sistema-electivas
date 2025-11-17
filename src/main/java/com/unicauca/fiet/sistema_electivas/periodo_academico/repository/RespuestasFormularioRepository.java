package com.unicauca.fiet.sistema_electivas.periodo_academico.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoRespuestaFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RespuestasFormularioRepository  extends JpaRepository<RespuestasFormulario, Long> {
    /**
     * Recupera todas las respuestas asociadas a un período académico,
     * cargando de forma anticipada (fetch) las relaciones necesarias
     * para evitar problemas de rendimiento (N+1 queries).
     */
    @EntityGraph(attributePaths = {
            "programa",                  // carga el programa del estudiante
            "periodo",                   // carga el período académico
            "opciones",                  // carga las opciones elegidas
            "opciones.oferta",           // carga la oferta de cada opción
            "opciones.oferta.electiva"   // carga la electiva asociada a la oferta
    })
    List<RespuestasFormulario> findByPeriodoId(Long periodoId);

    /**
     * Obtiene todas las respuestas que se encuentren en un estado específico,
     * sin filtrarlas por período académico.
     *
     * @param estadoRespuestaFormulario estado requerido.
     * @return lista de respuestas que coinciden con el estado.
     */
    List<RespuestasFormulario> findByEstado(EstadoRespuestaFormulario estadoRespuestaFormulario);

    /**
     * Obtiene todas las respuestas de un período académico filtradas por un estado
     * específico.
     *
     * Genera internamente:
     * <pre>
     * SELECT r
     * FROM RespuestasFormulario r
     * WHERE r.periodo.id = :periodoId
     *   AND r.estado = :estado
     * </pre>
     *
     * @param periodoId ID del período.
     * @param estado estado de la respuesta.
     * @return lista filtrada de respuestas.
     */
    List<RespuestasFormulario> findByPeriodoIdAndEstado(Long periodoId, EstadoRespuestaFormulario estado);

    /**
     * Devuelve los códigos de estudiante válidos (por ejemplo, CUMPLE o INCLUIDO_MANUAL)
     * para un periodo académico específico.
     */
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
    /**
     * Devuelve la cantidqd de códigos de estudiante ven los estados que se pidan
     * para un periodo académico específico.
     */
    long countByPeriodoIdAndEstadoIn(Long periodoId, List<EstadoRespuestaFormulario> estados);
    List<RespuestasFormulario> findByPeriodoIdAndEstadoIn(Long periodoId, List<EstadoRespuestaFormulario> estados);

    /**
     * Verifica si existe una respuesta para un período académico con un código
     * de estudiante específico, siempre que la respuesta se encuentre en el estado
     * indicado.
     *
     * <p>Usado para evitar duplicados al corregir inconsistencias o cargar datos
     * manualmente.</p>
     *
     * @param periodoId ID del período.
     * @param codigoEstudiante código del estudiante.
     * @param estado estado a validar.
     * @return true si existe una coincidencia.
     */
    boolean existsByPeriodoIdAndCodigoEstudianteAndEstado(
            Long periodoId,
            String codigoEstudiante,
            EstadoRespuestaFormulario estado
    );

    /**
     * Verifica si existe al menos una respuesta para un período académico
     * que se encuentre en alguno de los estados proporcionados.
     *
     * <p>Usado para evaluar si un período puede avanzar al siguiente estado.</p>
     *
     * @param periodoId ID del período académico.
     * @param estados lista de estados válidos.
     * @return true si hay alguna respuesta en esos estados.
     */
    boolean existsByPeriodoIdAndEstadoIn(Long periodoId, List<EstadoRespuestaFormulario> estados);


}
