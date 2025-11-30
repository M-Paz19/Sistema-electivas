package com.unicauca.fiet.sistema_electivas.reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO que representa la popularidad de una electiva específica.
 *
 * <p>Incluye:</p>
 * <ul>
 *     <li>Nombre de la electiva.</li>
 *     <li>Conteo de estudiantes que la escogieron por cada número de opción (1,2,3...).</li>
 *     <li>Total de estudiantes que la eligieron en cualquier opción.</li>
 * </ul>
 *
 * Se utiliza dentro de {@link PopularidadElectivasResponse} para mostrar la distribución por electiva.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PopularidadElectivaDto {

    /** Nombre de la electiva */
    private String nombre;

    /**
     * Mapa:
     *  - key = número de opción (1,2,3...)
     *  - value = cantidad de estudiantes que la escogieron así
     */
    private Map<Integer, Integer> conteoPorOpcion;

    /** Total de estudiantes que la eligieron en cualquier opción */
    private Integer conteoTotal;
}
