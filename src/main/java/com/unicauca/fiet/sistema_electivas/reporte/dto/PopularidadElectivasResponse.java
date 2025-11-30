package com.unicauca.fiet.sistema_electivas.reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que representa la respuesta de popularidad de electivas para un semestre específico.
 *
 * <p>Incluye:</p>
 * <ul>
 *     <li>El semestre correspondiente (ejemplo: "2025-1").</li>
 *     <li>Lista de electivas y la distribución de estudiantes por opción.</li>
 * </ul>
 *
 * Se utiliza para mostrar estadísticas consolidadas de demanda de electivas.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PopularidadElectivasResponse {

    /** Ejemplo: "2025-1" */
    private String semestre;

    /** Lista de electivas y su distribución por opción */
    private List<PopularidadElectivaDto> electivas;
}
