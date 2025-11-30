package com.unicauca.fiet.sistema_electivas.reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para enviar parámetros de búsqueda de popularidad de electivas.
 *
 * <p>Permite filtrar la popularidad por:</p>
 * <ul>
 *     <li>código de estudiante</li>
 *     <li>número de opción de electiva</li>
 *     <li>nombre de la electiva</li>
 * </ul>
 *
 * Se utiliza típicamente en requests hacia endpoints que generan estadísticas de popularidad.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PopularidadRequestDto {

    private String estudianteCodigo;

    private Integer numeroOpcion;

    private String nombreElectiva;
}
