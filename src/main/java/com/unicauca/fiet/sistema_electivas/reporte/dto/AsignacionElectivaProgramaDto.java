package com.unicauca.fiet.sistema_electivas.reporte.dto;

import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la información de una asignación de electiva de un estudiante
 * junto con el programa académico al que pertenece.
 *
 * <p>Incluye:</p>
 * <ul>
 *     <li>{@code estudianteCodigo}: código del estudiante.</li>
 *     <li>{@code numeroOpcion}: número de opción con la que se asignó la electiva.</li>
 *     <li>{@code estado}: estado de la asignación ({@link EstadoAsignacion}).</li>
 *     <li>{@code programaCodigo}: código del programa académico del estudiante.</li>
 * </ul>
 *
 * <p>Se utiliza para reportes o análisis donde se requiere conocer el estado de la asignación
 * y a qué programa pertenece el estudiante.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignacionElectivaProgramaDto {

    private String estudianteCodigo;

    private Integer numeroOpcion;

    private EstadoAsignacion estado;

    private String programaCodigo;
}
