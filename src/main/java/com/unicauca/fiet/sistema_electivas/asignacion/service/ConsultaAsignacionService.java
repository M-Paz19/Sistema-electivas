package com.unicauca.fiet.sistema_electivas.asignacion.service;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.DepartamentoReporteDTO;
import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteAsignacionReporteResponse;
import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteOrdenamientoResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;

import java.util.List;

public interface ConsultaAsignacionService {


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

    /**
     * Genera la estructura base del reporte de asignación, organizada por
     * departamentos y electivas, incluyendo:
     * <ul>
     *   <li>Información del departamento</li>
     *   <li>Ofertas del período agrupadas por departamento</li>
     *   <li>Programas asociados a cada electiva</li>
     *   <li>Listas de estudiantes asignados y en espera, ordenados oficialmente</li>
     * </ul>
     *
     * <p>Este reporte es utilizado como insumo para la generación del PDF
     * final o para exportaciones administrativas.</p>
     *
     * @param periodoId ID del período académico en estado ASIGNACION_PROCESADA
     * @return lista estructurada de departamentos con sus ofertas y estudiantes
     */
    List<DepartamentoReporteDTO> generarListasDeAsigancionPorDepartamentos(Long periodoId);

    /**
     * Genera el reporte final de ranking de asignación por estudiante,
     * aplicando los criterios oficiales de ordenamiento y construyendo
     * un DTO detallado por cada estudiante apto.
     *
     * <p>Por cada estudiante se incluye:</p>
     * <ul>
     *     <li>Datos académicos generales</li>
     *     <li>Métricas de avance y promedio</li>
     *     <li>Cantidad de electivas aprobadas, faltantes y requeridas</li>
     *     <li>Electivas asignadas y en lista de espera</li>
     *     <li>Programas que pueden cursar cada electiva asignada</li>
     * </ul>
     *
     * <p>El proceso consiste en:</p>
     * <ol>
     *     <li>Verificar que el período esté en estado ASIGNACION_PROCESADA</li>
     *     <li>Obtener los estudiantes aptos</li>
     *     <li>Ordenarlos según criterios oficiales</li>
     *     <li>Obtener sus asignaciones individualmente</li>
     *     <li>Construir el DTO mediante el mapper especializado</li>
     * </ol>
     *
     * <p>Este reporte es utilizado para visualización administrativa
     * o como insumo para generar exportaciones PDF/Excel.</p>
     *
     * @param periodoId identificador del período académico
     * @return lista ordenada de estudiantes y sus asignaciones
     */
    List<EstudianteAsignacionReporteResponse> generarReporteRanking(Long periodoId);


}
