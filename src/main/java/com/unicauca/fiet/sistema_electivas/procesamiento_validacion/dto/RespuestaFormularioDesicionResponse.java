package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.Builder;
import lombok.Data;

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
