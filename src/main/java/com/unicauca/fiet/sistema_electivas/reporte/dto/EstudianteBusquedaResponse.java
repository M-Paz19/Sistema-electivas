package com.unicauca.fiet.sistema_electivas.reporte.dto;

import lombok.Data;

/**
 * DTO que representa el historial académico y de asignaciones de un estudiante
 * para un período académico específico.
 *
 * <p>Incluye:</p>
 * <ul>
 *     <li>El período académico al que corresponde la información.</li>
 *     <li>Los datos académicos del estudiante en ese período.</li>
 *     <li>Las respuestas de formularios enviadas por el estudiante.</li>
 *     <li>Las asignaciones de electivas del estudiante en ese período.</li>
 *     <li>Totales de asignaciones con estado ASIGNADA y LISTA_ESPERA para
 *         facilitar reportes y análisis rápidos.</li>
 * </ul>
 *
 * <p>Se utiliza principalmente para mostrar el historial completo de un estudiante
 * por período, integrando información académica y de asignaciones.</p>
 */
@Data
public class EstudianteBusquedaResponse {
    private String codigoEstudiante;
    private String nombres;
    private String apellidos;
    private String programa;
}
