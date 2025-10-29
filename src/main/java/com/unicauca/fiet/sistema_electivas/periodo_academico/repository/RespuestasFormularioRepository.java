package com.unicauca.fiet.sistema_electivas.periodo_academico.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
