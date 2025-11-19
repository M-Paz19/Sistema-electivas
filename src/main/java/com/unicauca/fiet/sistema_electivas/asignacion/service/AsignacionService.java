package com.unicauca.fiet.sistema_electivas.asignacion.service;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteOrdenamientoResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CambioEstadoValidacionResponse;

import java.util.List;

public interface AsignacionService {
    /**
     * HU 3.1.1: Filtra estudiantes no elegibles para asignación de electivas,
     * específicamente aquellos que ya cursaron todas las electivas disponibles.
     *
     * @param periodoId ID del período académico.
     * @return Resumen del proceso con estado actualizado.
     */
    CambioEstadoValidacionResponse filtrarEstudiantesNoElegibles(Long periodoId);

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
}
