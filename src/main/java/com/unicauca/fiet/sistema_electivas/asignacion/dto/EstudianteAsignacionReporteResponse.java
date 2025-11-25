package com.unicauca.fiet.sistema_electivas.asignacion.dto;

import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO que representa la información consolidada de un estudiante,
 * incluyendo tanto sus métricas académicas oficiales como el resultado
 * de sus asignaciones de electivas.
 *
 * <p>Se utiliza para generar reportes internos del proceso de asignación,
 * mostrando el ordenamiento oficial y el estado final de cada asignación,
 * así como métricas de créditos y conteos de asignaciones.</p>
 */
@Data
public class EstudianteAsignacionReporteResponse {

    // =====================
    // Datos básicos del estudiante
    // =====================
    private Long id;
    private String codigoEstudiante;
    private String apellidos;
    private String nombres;
    private String usuario;
    private String programa;

    // =====================
    // Métricas de créditos
    // =====================
    private Integer creditosAprobadosTotal;       // Total de créditos aprobados
    private Integer creditosAprobadosObligatorio; // Créditos aprobados de materias obligatorias
    private Integer creditosPensumObligatorio;    // Total de créditos obligatorios en el plan de estudios

    // =====================
    // Métricas académicas
    // =====================
    private Integer periodosMatriculados;
    private Boolean esNivelado;
    private BigDecimal porcentajeAvance;
    private BigDecimal promedioCarrera;
    private Integer debeVer;
    private Integer aprobadas;
    private Integer faltan;

    // =====================
    // Conteo de asignaciones
    // =====================
    private Integer asignadas;      // Cantidad de asignaciones asignadas
    private Integer listaDeEspera;  // Cantidad de asignaciones en lista de espera

    // =====================
    // Resultado de las asignaciones
    // =====================
    private List<AsignacionElectivaInfo> asignaciones;

    /**
     * Información de cada asignación de electiva para el estudiante.
     */
    @Data
    public static class AsignacionElectivaInfo {
        private Integer numeroOpcion;
        private String nombreElectiva;  // Nombre de la electiva
        private EstadoAsignacion estado; // ASIGNADO o EN_ESPERA
    }
}
