package com.unicauca.fiet.sistema_electivas.asignacion.dto;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO que representa la información consolidada de un estudiante
 * para los procesos de ordenamiento y priorización de electivas.
 *
 * <p>Incluye tanto datos personales y académicos como métricas
 * calculadas necesarias para determinar su posición en el orden
 * de asignación de cupos.</p>
 *
 * <p>Se utiliza en las respuestas del servicio encargado de
 * obtener los estudiantes aptos ordenados por los criterios
 * oficiales definidos por la universidad.</p>
 */
@Data
public class EstudianteOrdenamientoResponse {

    private Long id;
    private String codigoEstudiante;

    private String apellidos;
    private String nombres;
    private String usuario;
    private Integer creditosAprobados;
    private String programa;

    private Integer periodosMatriculados;
    private BigDecimal porcentajeAvance;
    private BigDecimal promedioCarrera;

    private Integer debeVer;
    private Integer aprobadas;
    private Integer faltan;
    private Boolean esNivelado;


    private EstadoAptitud estadoAptitud;
}
