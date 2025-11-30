package com.unicauca.fiet.sistema_electivas.reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO que representa la distribución de asignaciones de electivas por cantidad de electivas
 * elegidas por los estudiantes en un semestre académico específico.
 *
 * <p>Incluye conteos de estudiantes según el número de electivas:</p>
 * <ul>
 *     <li>{@code asignadas}: número de estudiantes con asignaciones confirmadas por cantidad de electivas (0,1,2,...,5+).</li>
 *     <li>{@code listaEspera}: número de estudiantes en lista de espera por cantidad de electivas (0,1,2,...,5+).</li>
 *     <li>{@code total}: suma de asignadas y lista de espera por cantidad de electivas.</li>
 * </ul>
 *
 * <p>Se utiliza para generar reportes estadísticos de asignaciones y demanda de electivas
 * por semestre.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistribucionAsignacionesResponse {

    private String semestre;

    private Map<String, Long> asignadas;         // 0,1,2,3,4,5+
    private Map<String, Long> listaEspera;       // 0,1,2,3,4,5+
    private Map<String, Long> total;             // asignadas + espera
}
