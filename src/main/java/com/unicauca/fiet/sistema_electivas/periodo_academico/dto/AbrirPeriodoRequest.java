package com.unicauca.fiet.sistema_electivas.periodo_academico.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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
     * Número de opciones que puede escoger cada programa.
     * Key = programaId, Value = cantidad de opciones.
     */
    @NotNull(message = "Debe enviar las opciones por programa.")
    private Map<Long, Integer> opcionesPorPrograma;

    /**
     * Indica si se debe forzar la apertura del período antes de la fecha configurada.
     * Por defecto es false.
     */
    private boolean forzarApertura = false;
}
