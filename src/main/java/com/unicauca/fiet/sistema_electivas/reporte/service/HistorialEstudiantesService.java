package com.unicauca.fiet.sistema_electivas.reporte.service;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import com.unicauca.fiet.sistema_electivas.reporte.dto.EstudianteBusquedaResponse;
import com.unicauca.fiet.sistema_electivas.reporte.dto.HistorialEstudiantePeriodoResponse;

import java.util.List;

public interface HistorialEstudiantesService {

    /**
     * Obtiene todo el historial del estudiante agrupado por período.
     *
     * @param codigoEstudiante código del estudiante.
     * @return lista de períodos con datos académicos, respuestas y asignaciones.
     */
    List<HistorialEstudiantePeriodoResponse> obtenerHistorialPorEstudiante(String codigoEstudiante);

    /**
     * Busca estudiantes por coincidencias en código, nombres o apellidos.
     *
     * <p>La búsqueda se realiza tanto en la entidad {@link DatosAcademico} como en
     * {@link RespuestasFormulario} para asegurar que se incluyan todos los registros
     * existentes, incluso si solo existen en una de las dos tablas.</p>
     *
     * <p>Los resultados devueltos son combinados y se eliminan duplicados basados
     * en el {@code codigoEstudiante}. Solo se devuelve información mínima:</p>
     * <ul>
     *     <li>codigoEstudiante</li>
     *     <li>nombres</li>
     *     <li>apellidos</li>
     *     <li>programa</li>
     * </ul>
     *
     * @param filtro texto a buscar en código, nombres o apellidos del estudiante
     * @return lista de {@link EstudianteBusquedaResponse} con coincidencias únicas
     */
    List<EstudianteBusquedaResponse> buscar(String filtro);
}
