package com.unicauca.fiet.sistema_electivas.periodo_academico.enums;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum que representa los posibles estados de un {@link PeriodoAcademico}.
 *
 * <ul>
 *   <li>EN_PREPARACION: El periodo está creado pero aún no ha iniciado.</li>
 *   <li>ACTIVO: El periodo se encuentra en curso.</li>
 *   <li>CERRADO: El periodo ha finalizado y no admite más modificaciones.</li>
 * </ul>
 */
public enum EstadoPeriodoAcademico {

    CONFIGURACION("Configuración inicial del periodo", false),
    ABIERTO_FORMULARIO("Formulario abierto para estudiantes", true),
    CERRADO_FORMULARIO("Formulario cerrado - respuestas en revisión", true),

    PROCESO_FILTRADO_DUPLICADOS("Aplicado filtro de duplicados", true),
    PROCESO_CLASIFICACION_ANTIGUEDAD("Codigos formulario validados por formato y antiguedad", true),
    PROCESO_CONFIRMACION_SIMCA("Confirmación final para SIMCA", true),
    PROCESO_CARGA_SIMCA("Carga de datos SIMCA", true),

    PROCESO_REVISION_POTENCIALES_NIVELADOS("Revisión de posibles nivelados (preselección automática)", true),
    PROCESO_CALCULO_AVANCE("Cálculo del porcentaje de avance académico de todos los estudiantes", true),
    PROCESO_CALCULO_APTITUD("Determinación final de aptitud para asignación de electivas", true),


    EN_PROCESO_ASIGNACION("Asignación de electivas en curso", true),
    CERRADO("Periodo cerrado - asignaciones completadas", false);

    private final String descripcion;
    private final boolean activo;

    EstadoPeriodoAcademico(String descripcion, boolean activo) {
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esActivo() {
        return activo;
    }

    public static List<EstadoPeriodoAcademico> obtenerEstadosActivos() {
        return Arrays.stream(values())
                .filter(EstadoPeriodoAcademico::esActivo)
                .collect(Collectors.toList());
    }
}