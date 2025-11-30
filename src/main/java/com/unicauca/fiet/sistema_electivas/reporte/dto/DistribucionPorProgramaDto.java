package com.unicauca.fiet.sistema_electivas.reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO que representa la distribución de asignaciones de electivas
 * para un programa académico específico.
 *
 * <p>Incluye conteos de estudiantes según el estado de la asignación:</p>
 * <ul>
 *     <li>{@code asignadas}: número de estudiantes con asignación confirmada.</li>
 *     <li>{@code listaEspera}: número de estudiantes en lista de espera.</li>
 *     <li>{@code total}: total de estudiantes que se les asigno ya sea asignacion directa o lista de espera alguna opción de la electiva.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistribucionPorProgramaDto {

    private Map<String, Long> asignadas;
    private Map<String, Long> listaEspera;
    private Map<String, Long> total;
}
