package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.Builder;
import lombok.Data;
/**
 * DTO de respuesta para decisiones tomadas sobre formularios o evaluaciones individuales.
 *
 * <p>Se utiliza generalmente para devolver el resultado de una acción del usuario,
 * como la aprobación o rechazo de un formulario.</p>
 */
@Data
@Builder
public class RespuestaFormularioDesicionResponse {
    private Long id;
    private String codigoEstudiante;
    private String correoEstudiante;
    private String nombreCompleto;
    private String estado;
    private String mensaje;
}
