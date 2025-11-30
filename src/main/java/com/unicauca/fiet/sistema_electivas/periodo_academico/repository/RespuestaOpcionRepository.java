package com.unicauca.fiet.sistema_electivas.periodo_academico.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestaOpcion;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaOpcionRepository extends JpaRepository<RespuestaOpcion, Long> {
    /**
     * Obtiene todas las opciones seleccionadas en una respuesta específica,
     * ordenadas por el número de opción ascendentemente.
     *
     * <p>Útil para presentar las opciones del estudiante en el mismo orden
     * en que fueron registradas (1, 2, 3...).</p>
     *
     * @param respuesta entidad RespuestasFormulario a la cual pertenecen las opciones.
     * @return lista de opciones ordenadas por el campo opcionNum.
     */
    List<RespuestaOpcion> findByRespuestaOrderByOpcionNumAsc(RespuestasFormulario respuesta);

    /**
     * Recupera todas las opciones de respuesta pertenecientes a un período académico
     * cuyo estado sea <strong>DATOS_CARGADOS</strong>.
     *
     * <p>Este método se utiliza principalmente durante fases de validación o depuración,
     * ya que permite obtener todas las opciones enviadas por estudiantes antes de que
     * sus respuestas sean procesadas o evaluadas.</p>
     *
     * <p>Los resultados se ordenan primero por el ID de la respuesta y luego por el
     * número de opción dentro de cada respuesta.</p>
     *
     * @param periodoId ID del período académico.
     * @return lista de RespuestaOpcion ordenadas por respuesta y número de opción.
     */
    @Query("""
        SELECT ro
        FROM RespuestaOpcion ro
        JOIN ro.respuesta r
        WHERE r.periodo.id = :periodoId
          AND r.estado = com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoRespuestaFormulario.DATOS_CARGADOS
        ORDER BY r.id ASC, ro.opcionNum ASC
    """)
    List<RespuestaOpcion> findAllOpcionesByPeriodoAndEstadoDatosCargados(Long periodoId);
}
