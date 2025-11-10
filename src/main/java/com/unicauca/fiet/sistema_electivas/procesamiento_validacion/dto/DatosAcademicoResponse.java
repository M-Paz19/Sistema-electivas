package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO de respuesta para la entidad DatosAcademico.
 * Se usa para mostrar el listado de 'Posibles Nivelados' (HU 2.2.1.2)
 * y otras consultas de datos académicos.
 */
@Data
public class DatosAcademicoResponse {

    private Long id;
    private String codigoEstudiante;
    private String apellidos;
    private String nombres;
    private String programa;
    private Integer creditosAprobados;
    private Integer periodosMatriculados;
    private BigDecimal promedioCarrera;

    private Integer aprobadas;

    private Boolean esNivelado;
    private BigDecimal porcentajeAvance;

    /** Estado del proceso de validación académica (PENDIENTE, POSIBLE_NIVELADO, etc.) */
    private EstadoAptitud estadoAptitud;
}