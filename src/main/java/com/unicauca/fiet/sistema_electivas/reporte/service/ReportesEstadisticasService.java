package com.unicauca.fiet.sistema_electivas.reporte.service;

import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.reporte.dto.*;

public interface ReportesEstadisticasService {
    /**
     * Obtiene la distribución de cuántas electivas fueron asignadas
     * a cada estudiante en un período académico específico.
     * HU 4.1 – Distribución de electivas asignadas por estudiante.
     *
     * @param periodoId ID del período académico
     * @return objeto con la distribución general (asignadas, lista de espera, total)
     */
    DistribucionAsignacionesResponse obtenerDistribucionAsignaciones(Long periodoId);
    /**
     * Obtiene la distribución de asignaciones agrupadas por programa académico
     * para un período académico dado.
     *
     * HU 4.2 – Distribución de electivas asignadas por programa.
     *
     * @param periodoId ID del período académico
     * @return objeto que contiene la cantidad de asignaciones por programa
     */
    DistribucionAsignacionesPorProgramaResponse obtenerDistribucionPorPrograma(Long periodoId);

    /**
     * Construye un resumen unificado de estados provenientes de dos fuentes:
     * el formulario de inscripción y el proceso de aptitud.
     *
     * <p>Este método toma los conteos generados para cada estado en:
     * <ul>
     *   <li><b>EstadoRespuestaFormulario</b>: solo se incluyen los estados
     *       {@code DUPLICADO}, {@code NO_CUMPLE}, {@code DESCARTADO},
     *       {@code DESCARTADO_SIMCA} y {@code DATOS_CARGADOS}.</li>
     *   <li><b>EstadoAptitud</b>: solo se incluyen los estados
     *       {@code NO_APTO}, {@code EXCLUIDO_POR_ELECTIVAS} y
     *       {@code ASIGNACION_PROCESADA}.</li>
     * </ul>
     *
     * <p>Por cada estado permitido se genera un {@link ResumenEstadoItem}
     * indicando:
     * <ul>
     *   <li>El origen del estado: {@code "FORMULARIO"} o {@code "APTITUD"}.</li>
     *   <li>El nombre del estado.</li>
     *   <li>La descripción legible del estado.</li>
     *   <li>La cantidad de registros asociados a dicho estado.</li>
     * </ul>
     *
     * <p>El resultado final es una lista consolidada que permite visualizar,
     * en un solo bloque, la distribución combinada de ambos procesos.
     *
     * @return lista unificada de items de resumen filtrados y enriquecidos.
     */
    ResumenProcesamientoPeriodoResponse obtenerResumenProcesamiento(Long periodoId);

    /**
     * Genera el reporte general de distribución en formato Excel para un período académico.
     *
     * <p>Este reporte incluye:
     * <ul>
     *   <li>Distribución completa de asignaciones por electiva.</li>
     *   <li>Distribución agrupada por programa académico.</li>
     *   <li>Resumen del procesamiento del período (totales, estados, métricas).</li>
     * </ul>
     *
     * <p>El método obtiene todos los datos desde el módulo Java
     * y delega la generación del archivo Excel al microservicio Python.
     *
     * @param periodoId ID del período académico para el cual se genera el reporte.
     * @return Un arreglo de bytes que representa el archivo Excel generado.
     * @throws ResourceNotFoundException Si el período no existe.
     * @throws InvalidStateException Si el período aún no tiene el procesamiento listo.
     */
    byte[] generarReporteDistribucionExcel(Long periodoId);

    /**
     * Calcula la popularidad de las electivas para un período académico.
     *
     * <p>Este proceso:
     * <ol>
     *   <li>Valida que el período ya tenga la asignación procesada.</li>
     *   <li>Obtiene las respuestas válidas de los estudiantes (sin duplicados y solo aptos).</li>
     *   <li>Construye un DTO estándar con cada opción seleccionada.</li>
     *   <li>Envía la lista al microservicio Python para calcular:
     *       <ul>
     *         <li>Conteo total de selecciones por electiva.</li>
     *         <li>Distribución por número de opción (1ra, 2da, 3ra, ...).</li>
     *         <li>Ordenamiento de electivas por popularidad.</li>
     *       </ul>
     *   </li>
     *   <li>Retorna la respuesta con el semestre asociado.</li>
     * </ol>
     *
     * @param periodoId ID del período académico del cual se desea obtener la popularidad.
     * @return Objeto {@link PopularidadElectivasResponse} con el ranking de electivas.
     * @throws ResourceNotFoundException Si el período no existe.
     * @throws InvalidStateException Si el período aún no está listo para consulta.
     */
    PopularidadElectivasResponse obtenerPopularidad(Long periodoId);

    /**
     * Obtiene la popularidad de las electivas tomando en cuenta todas las respuestas
     * válidas y también las descartadas por criterios académicos, pero excluyendo
     * respuestas duplicadas. Luego delega el cálculo estadístico al microservicio Python.
     *
     * Esta variante considera los estados finales:
     *  - NO_CUMPLE
     *  - DESCARTADO
     *  - DESCARTADO_SIMCA
     *  - DATOS_CARGADOS
     *
     * Pero excluye:
     *  - DUPLICADO (no deben contarse múltiples envíos)
     *
     * @param periodoId ID del periodo académico
     * @return respuesta con el ranking de popularidad
     */
    PopularidadElectivasResponse obtenerPopularidadIncluyendoDescartados(Long periodoId);

    /**
     * Genera el archivo Excel que consolida la popularidad de selección
     * de electivas para un período académico.
     * <p>
     * El reporte contiene dos hojas:
     * <ul>
     *     <li><b>Aptos:</b> Popularidad considerando únicamente estudiantes válidos (sin descartados).</li>
     *     <li><b>Incluyendo descartados:</b> Popularidad total incluyendo respuestas que fueron descartadas.</li>
     * </ul>
     * <p>
     * Este método:
     * <ol>
     *     <li>Obtiene la popularidad basada solo en estudiantes aptos.</li>
     *     <li>Obtiene la popularidad incluyendo estudiantes descartados.</li>
     *     <li>Envía ambos conjuntos de datos al microservicio Python, que construye el archivo Excel.</li>
     * </ol>
     *
     * @param periodoId ID del período académico a procesar.
     * @return El archivo Excel en formato <code>byte[]</code> listo para descarga.
     */
    byte[] generarReportePopularidadExcel(Long periodoId);
}
