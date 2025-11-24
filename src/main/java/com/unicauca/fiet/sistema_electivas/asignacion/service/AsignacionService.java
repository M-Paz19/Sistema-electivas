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
     * Procesa la asignación masiva de todos los estudiantes aptos de un período académico,
     * aplicando el algoritmo oficial de asignación de electivas.
     *
     * <p>El flujo general del método es el siguiente:</p>
     * <ol>
     *     <li>Valida que el período exista y que su estado sea {@code EN_PROCESO_ASIGNACION}.</li>
     *     <li>Obtiene y prepara en memoria todos los datos necesarios para la asignación:
     *         estudiantes aptos, respuestas de formularios, opciones seleccionadas,
     *         ofertas disponibles y cupos por programa/oferta.</li>
     *     <li>Recorre la lista de estudiantes aptos y ejecuta la asignación individual
     *         mediante la funcion procesar un estudiante para cada estudiante.</li>
     *     <li>Registra errores de procesamiento por estudiante y mantiene un conteo de
     *         asignaciones exitosas y fallidas.</li>
     *     <li>Guarda todas las asignaciones realizadas en la base de datos y actualiza
     *         el estado del período a {@code ASIGNACION_PROCESADA}.</li>
     *     <li>Devuelve un objeto de respuesta con un resumen del proceso y el estado actualizado del período.</li>
     * </ol>
     *
     * <p>Nota: Este método es transaccional; cualquier error en la ejecución individual de un
     * estudiante no interfiere con el procesamiento de los demás.</p>
     *
     * @param periodoId ID del período académico a procesar.
     * @return {@link CambioEstadoValidacionResponse} que contiene el estado final del período
     *         y un mensaje resumen del proceso.
     */
    CambioEstadoValidacionResponse procesarAsignacionMasiva(Long periodoId);
}
