package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa una electiva seleccionada por un estudiante
 * dentro de una respuesta de formulario.
 *
 * <p>Incluye el nombre de la electiva y el número de opción
 * con el que fue seleccionada (1, 2, 3, ...).</p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ElectivaSeleccionadaResponse {
    private Short opcionNum;     // número de opción (1, 2, 3…)
    private String nombreElectiva; // nombre de la electiva ofertada
}