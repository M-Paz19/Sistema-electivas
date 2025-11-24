package com.unicauca.fiet.sistema_electivas.asignacion.service;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteOrdenamientoResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;

import java.util.List;

public interface ConsultaAsignacionService {

    /**
     * Obtiene todas las asignaciones de un período académico.
     * 
     * @param periodoId ID del período
     * @return Lista de DTOs con la información de asignaciones
     */
    //List<AsignacionElectivaResponse> obtenerAsignacionesPorPeriodo(Long periodoId);

    /**
     * Obtiene las asignaciones de un estudiante específico.
     * 
     * @param codigoEstudiante Código del estudiante
     * @param periodoId ID del período
     * @return Lista de DTOs con asignaciones del estudiante
     */
    //List<AsignacionElectivaResponse> obtenerAsignacionesPorEstudiante(String codigoEstudiante, Long periodoId);

    /**
     * Obtiene todos los estudiantes cuyo estado de aptitud es APTO dentro
     * de un período académico, y los ordena aplicando los criterios oficiales:
     * <ol>
     *   <li>Porcentaje de avance (DESC)</li>
     *   <li>Promedio de carrera (DESC)</li>
     *   <li>Electivas faltantes (ASC)</li>
     * </ol>
     *
     * <p>El resultado es una lista de DTOs creada especialmente para mostrar
     * en frontend todos los datos necesarios para la etapa de asignación.</p>
     *
     * @param periodoId ID del período académico en proceso
     * @return Lista ordenada de estudiantes aptos, lista para asignación
     */
    List<EstudianteOrdenamientoResponse> obtenerAptosOrdenados(Long periodoId);

    /**
     * Obtiene y ordena internamente la lista de estudiantes aptos
     * aplicando los criterios oficiales establecidos por la universidad.
     *
     * <p>Este método es auxiliar y su propósito es permitir que otras
     * operaciones internas del servicio (como la asignación de cupos)
     * trabajen directamente con las entidades {@link DatosAcademico},
     * evitando transformaciones innecesarias a DTOs.</p>
     *
     * @param periodoId identificador del período académico
     * @return lista ordenada de entidades {@link DatosAcademico}
     */
    List<DatosAcademico> obtenerAptosOrdenadosInterno(Long periodoId);

}
