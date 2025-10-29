package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioResponse;

import java.util.List;

public interface ProcesamientoValidacionService {
    /**
     * Obtiene todas las respuestas asociadas a un período académico específico.
     *
     * @param periodoId ID del período académico
     * @return Lista de respuestas del formulario vinculadas a ese período.
     */
    List<RespuestaFormularioResponse> obtenerRespuestasPorPeriodo(Long periodoId);
}
