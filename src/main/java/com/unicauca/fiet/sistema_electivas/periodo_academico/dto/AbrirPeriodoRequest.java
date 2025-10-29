package com.unicauca.fiet.sistema_electivas.periodo_academico.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 DTO de petición utilizado para solicitar la apertura de un periodo academico y lanzar el formulario
 de inscripcion.

 <p>Contiene la información mínima necesaria para saber acerca de cambio de estado de un objeto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbrirPeriodoRequest {
    /**
     * Cantidad de opciones que los estudiantes podrán seleccionar
     * en el formulario de preinscripción. Debe ser al menos 1.
     */
    @NotNull(message = "El número de opciones del formulario es obligatorio.")
    @Min(value = 1, message = "El número de opciones del formulario debe ser al menos 1.")
    private Integer numeroOpcionesFormulario;

    /**
     * Indica si se debe forzar la apertura del período antes de la fecha configurada.
     * Por defecto es false.
     */
    private boolean forzarApertura = false;
}
