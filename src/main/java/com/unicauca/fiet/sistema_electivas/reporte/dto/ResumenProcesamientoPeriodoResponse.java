package com.unicauca.fiet.sistema_electivas.reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que representa un resumen del procesamiento de un período académico.
 *
 * <p>Incluye:</p>
 * <ul>
 *     <li>El ID del período procesado.</li>
 *     <li>El total de respuestas recibidas para ese período.</li>
 *     <li>Un listado de resúmenes por estado (Formulario o Aptitud),
 *         incluyendo cantidad y descripción de cada estado.</li>
 * </ul>
 *
 * <p>Se utiliza para reportes estadísticos de HU relacionadas con la
 * gestión de respuestas y validación académica de estudiantes.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenProcesamientoPeriodoResponse {

    private Long periodoId;

    private Long totalRespuestas;

    private List<ResumenEstadoItem> resumenEstados;  // <-- unificado
}
