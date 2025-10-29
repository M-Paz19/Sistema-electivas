package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * DTO de respuesta que representa los datos consolidados de una respuesta de formulario.
 * Incluye la información básica del estudiante, el semestre del período académico
 * y las electivas seleccionadas.
 */
@Getter
@Setter
@AllArgsConstructor
public class RespuestaFormularioResponse {

    private Long id;
    private String codigoEstudiante;
    private String correoEstudiante;
    private String nombreEstudiante;
    private String apellidosEstudiante;
    private String programaNombre;
    private String periodoSemestre;
    private Instant timestampRespuesta;
    // ahora lista de objetos, no de strings
    private List<ElectivaSeleccionadaResponse> electivasSeleccionadas;
}
