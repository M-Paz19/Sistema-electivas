package com.unicauca.fiet.sistema_electivas.periodo_academico.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestaOpcion;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaOpcionRepository extends JpaRepository<RespuestaOpcion, Long> {
    List<RespuestaOpcion> findByRespuestaOrderByOpcionNumAsc(RespuestasFormulario respuesta);
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
