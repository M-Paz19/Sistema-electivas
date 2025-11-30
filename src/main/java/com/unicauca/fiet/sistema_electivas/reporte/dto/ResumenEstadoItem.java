package com.unicauca.fiet.sistema_electivas.reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un ítem dentro del resumen de estados de procesamiento.
 *
 * <p>Cada ítem indica:</p>
 * <ul>
 *     <li>La categoría a la que pertenece (por ejemplo, FORMULARIO o APTITUD).</li>
 *     <li>El nombre del estado (como aparece en el enum correspondiente).</li>
 *     <li>Una descripción del estado.</li>
 *     <li>La cantidad de registros que se encuentran en ese estado.</li>
 * </ul>
 *
 * <p>Se utiliza dentro de {@link ResumenProcesamientoPeriodoResponse} para
 * consolidar la información de estados por período.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenEstadoItem {

    private String categoria;          // FORMULARIO o APTITUD
    private String estado;        // Nombre del enum
    private String descripcion;   // Descripción del enum
    private Long cantidad;
}
