package com.unicauca.fiet.sistema_electivas.reporte.dto;

import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la información mínima de una asignación de electiva
 * de un estudiante.
 *
 * <p>Incluye:</p>
 * <ul>
 *     <li>{@code estudianteCodigo}: código del estudiante.</li>
 *     <li>{@code numeroOpcion}: número de opción con la que se asignó la electiva.</li>
 *     <li>{@code estado}: estado de la asignación ({@link EstadoAsignacion}).</li>
 * </ul>
 *
 * <p>Se utiliza cuando solo se necesita información resumida de las asignaciones,
 * sin detalles adicionales de la electiva o del estudiante.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignacionElectivaMinDto {

    private String estudianteCodigo;

    private Integer numeroOpcion;

    private EstadoAsignacion estado;
}
