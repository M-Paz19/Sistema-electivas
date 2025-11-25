package com.unicauca.fiet.sistema_electivas.asignacion.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO que representa la información consolidada de una oferta de electiva
 * dentro del reporte general generado por departamento.
 *
 * <p>Incluye datos básicos de la electiva (código, nombre), la lista de
 * programas académicos a los que está asociada y, además, el conjunto
 * completo de estudiantes asignados o en lista de espera, debidamente
 * ordenados según los criterios oficiales definidos.</p>
 *
 * <p>Este DTO es utilizado como parte del árbol de respuesta del reporte
 * por departamento, permitiendo visualizar de forma estructurada las
 * ofertas abiertas en un período académico y la composición de sus
 * respectivos estudiantes.</p>
 */
@Data
public class OfertaReporteDTO {

    /** Identificador único de la oferta dentro del período académico. */
    private Long idOferta;

    /** Código oficial de la electiva asociada a la oferta. */
    private String codigoElectiva;

    /** Nombre oficial de la electiva asociada. */
    private String nombreElectiva;

    /**
     * Lista de nombres de los programas académicos que pueden tomar esta electiva.
     *
     * <p>Se utiliza solo el nombre para simplificar la representación,
     * ya que para efectos del reporte no se requiere un DTO más complejo.</p>
     */
    private List<String> programas;

    /**
     * Lista completa de estudiantes asignados o en lista de espera,
     * ordenados por porcentaje de avance, promedio y faltantes.
     *
     * <p>Cada elemento contiene datos personales del estudiante,
     * su estado dentro de la oferta y su posición numérica según
     * los criterios oficiales de priorización.</p>
     */
    private List<EstudianteAsignacionDTO> listaEstudiantes;
}
