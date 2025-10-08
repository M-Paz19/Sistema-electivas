package com.unicauca.fiet.sistema_electivas.periodo_academico.enums;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;

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
    CONFIGURACION("configuración"),
    ABIERTO("Abierto"),
    EN_PROCESO_ASIGNACION("En proceso de asignación"),
    CERRADO("Cerrado");

    private final String descripcion;

    EstadoPeriodoAcademico(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
