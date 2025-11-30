package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad {@link DatosAcademico}, con consultas derivadas
 * basadas en la relación hacia {@link RespuestasFormulario} y {@link PeriodoAcademico}.
 *
 * <p>Permite obtener registros de datos académicos filtrando por período
 * y/o por el estado de aptitud asignado durante el proceso de nivelación.</p>
 */
@Repository
public interface DatosAcademicoRepository extends JpaRepository<DatosAcademico, Long> {


    /**
     * Busca todos los datos académicos asociados a un período académico específico,
     * navegando a través de la entidad RespuestasFormulario a la que esta asociado.
     *
     * Esta consulta se traduce a:
     * "FROM DatosAcademico d WHERE d.respuestaFormulario.periodo.id = :periodoId"
     *
     * @param idPeriodo ID del PeriodoAcademico.
     * @return Lista de DatosAcademico cargados para ese período.
     */
    List<DatosAcademico> findByRespuesta_Periodo_Id(Long idPeriodo);

    /**
     * Verifica si existe al menos un estudiante en un período académico concreto
     * con un estado de aptitud específico.
     *
     * Spring Data genera una consulta equivalente a:
     * <pre>
     * SELECT CASE WHEN COUNT(d) > 0 THEN TRUE ELSE FALSE END
     * FROM DatosAcademico d
     * WHERE d.respuestaFormulario.periodo.id = :periodoId
     *   AND d.estadoAptitud = :estado
     * </pre>
     *
     * @param periodoId ID del período académico.
     * @param estado estado de aptitud a verificar.
     * @return true si existe al menos un registro en ese estado.
     */
    boolean existsByRespuesta_Periodo_IdAndEstadoAptitud(Long periodoId, EstadoAptitud estado);

    /**
     * Obtiene todos los registros de datos académicos de un período académico,
     * filtrados por una lista de estados de aptitud.
     *
     * Genera internamente una consulta equivalente a:
     * <pre>
     * SELECT d
     * FROM DatosAcademico d
     * WHERE d.respuestaFormulario.periodo.id = :periodoId
     *   AND d.estadoAptitud IN :estados
     * </pre>
     *
     * @param periodoId ID del período.
     * @param estados lista de estados de aptitud permitidos.
     * @return lista filtrada de estudiantes.
     */
    List<DatosAcademico> findByRespuesta_Periodo_IdAndEstadoAptitudIn(Long periodoId, List<EstadoAptitud> estados);

    /**
     * Variante equivalente a {@link #findByRespuesta_Periodo_Id(Long)}, pero usando
     * la convención de Spring sin subguión en el nombre de la propiedad.
     *
     * Este método existe para compatibilidad con otros repositorios del sistema.
     *
     * @param periodoId ID del periodo académico.
     * @return lista de datos académicos asociados al período.
     */
    List<DatosAcademico> findByRespuesta_PeriodoId(Long periodoId);

    /**
     * Variante equivalente a {@link #findByRespuesta_Periodo_IdAndEstadoAptitudIn(Long, List)},
     * pero usando la convención sin subguiones.
     *
     * Útil cuando se quiere mantener consistencia en el estilo de nombres
     * dentro de servicios que ya usan esta nomenclatura.
     *
     * @param periodoId ID del período académico.
     * @param estados lista de estados de aptitud permitidos.
     * @return lista filtrada de datos académicos.
     */
    List<DatosAcademico> findByRespuesta_PeriodoIdAndEstadoAptitudIn(
            Long periodoId,
            List<EstadoAptitud> estados
    );
    /**
     * Obtiene todos los registros de DatosAcadémicos asociados a un período académico
     * específico y que tengan exactamente el estado de aptitud indicado.
     *
     * <p>Esta consulta permite recuperar, por ejemplo, todos los estudiantes
     * con estado APTO, NO_APTO, EXCLUIDO_POR_ELECTIVAS, etc., pertenecientes
     * a un periodo dado.</p>
     *
     * @param periodoId       ID del período académico.
     * @param estadoAptitud   Estado de aptitud que se desea filtrar.
     * @return Lista de entidades {@link DatosAcademico} que cumplen el filtro.
     */
    List<DatosAcademico> findByRespuesta_PeriodoIdAndEstadoAptitud(
            Long periodoId,
            EstadoAptitud estadoAptitud
    );

    /**
     * Obtiene todos los registros de DatosAcadémicos asociados a un período académico
     * específico y que tengan exactamente el estado de aptitud indicado, pero cargando su plan de una vez.
     *
     * <p>Esta consulta permite recuperar, por ejemplo, todos los estudiantes
     * con estado APTO, NO_APTO, EXCLUIDO_POR_ELECTIVAS, etc., pertenecientes
     * a un periodo dado.</p>
     *
     * @param periodoId       ID del período académico.
     * @param estadoAptitud   Estado de aptitud que se desea filtrar.
     * @return Lista de entidades {@link DatosAcademico} que cumplen el filtro.
     */
    @Query("""
    SELECT d
    FROM DatosAcademico d
    JOIN FETCH d.planEstudios p
    JOIN FETCH d.respuesta r
    WHERE r.periodo.id = :periodoId
      AND d.estadoAptitud = :estadoAptitud
""")
    List<DatosAcademico> findAptosConPlanByPeriodo(
            @Param("periodoId") Long periodoId,
            @Param("estadoAptitud") EstadoAptitud estadoAptitud
    );

    /**
     * Busca los datos académicos de un estudiante para un período académico
     * específico.
     *
     * <p>Devuelve un único registro si existe, ya que cada estudiante solo
     * debe tener un conjunto de datos académicos por período.</p>
     *
     * @param codigo código del estudiante.
     * @param periodoId ID del período académico.
     * @return un Optional con los datos académicos si existen.
     */
    @Query("""
    SELECT d FROM DatosAcademico d
    WHERE d.codigoEstudiante = :codigo
      AND d.respuesta.periodo.id = :periodoId
    """)
    Optional<DatosAcademico> findByCodigoAndPeriodo(String codigo, Long periodoId);

    /**
     * Obtiene todos los registros académicos válidos para un período,
     * es decir, aquellos cuyo estado de aptitud ya fue procesado para asignación.
     *
     * <p>Incluye carga anticipada (fetch) de la respuesta y sus opciones,
     * evitando consultas adicionales posteriores.</p>
     *
     * @param periodoId ID del período académico.
     * @return lista de datos académicos válidos junto con sus respuestas.
     */
    @Query("""
    SELECT da 
    FROM DatosAcademico da
    JOIN FETCH da.respuesta r
    LEFT JOIN FETCH r.opciones o
    WHERE r.periodo.id = :periodoId
      AND da.estadoAptitud = 'ASIGNACION_PROCESADA'
    """)
    List<DatosAcademico> findValidosConOpcionesPorPeriodo(Long periodoId);

    /**
     * Recupera el historial de datos académicos de un estudiante a través
     * de todos los períodos en los que ha participado.
     *
     * <p>Los resultados se ordenan del período más reciente al más antiguo.</p>
     *
     * @param codigo código del estudiante.
     * @return lista de datos académicos ordenados cronológicamente.
     */
    @Query("""
    SELECT da
    FROM DatosAcademico da
    JOIN da.respuesta r
    JOIN r.periodo p
    WHERE da.codigoEstudiante = :codigo
    ORDER BY p.semestre DESC
    """)
    List<DatosAcademico> findHistorialDatosAcademicos(String codigo);

    /**
     * Busca coincidencias de estudiantes dentro de los datos académicos,
     * permitiendo filtro por código, nombres o apellidos.
     *
     * <p>La comparación se realiza de manera insensible a mayúsculas/minúsculas.</p>
     *
     * @param filtro texto parcial a buscar.
     * @return lista de coincidencias encontradas.
     */
    @Query("""
        SELECT d FROM DatosAcademico d
        WHERE LOWER(d.codigoEstudiante) LIKE LOWER(CONCAT('%', :filtro, '%'))
           OR LOWER(d.nombres) LIKE LOWER(CONCAT('%', :filtro, '%'))
           OR LOWER(d.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%'))
    """)
    List<DatosAcademico> buscarCoincidencias(String filtro);

}

