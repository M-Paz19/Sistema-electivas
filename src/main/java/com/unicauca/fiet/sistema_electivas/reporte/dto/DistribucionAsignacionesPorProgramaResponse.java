package com.unicauca.fiet.sistema_electivas.reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO que representa la distribución de asignaciones de electivas por programas
 * para un semestre académico específico.
 *
 * <p>Incluye un mapeo de programas a su distribución de asignaciones.</p>
 * <ul>
 *     <li>{@code semestre}: semestre académico correspondiente (ej. "2025-1").</li>
 *     <li>{@code programas}: un mapa donde la clave es el nombre del programa
 *         y el valor es la distribución de asignaciones de ese programa ({@link DistribucionPorProgramaDto}).</li>
 * </ul>
 *
 * <p>Se utiliza para reportes estadísticos de asignaciones de electivas por programa.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistribucionAsignacionesPorProgramaResponse {

    private String semestre;

    // Un programa → una distribución
    private Map<String, DistribucionPorProgramaDto> programas;
}
