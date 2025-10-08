package com.unicauca.fiet.sistema_electivas.departamento.enums;

import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;

/**
 * Enum que representa los posibles estados de un {@link Departamento}.
 *
 * <ul>
 *   <li><b>ACTIVO:</b> El departamento está habilitado y puede asociarse a programas o electivas.</li>
 *   <li><b>INACTIVO:</b> El departamento fue deshabilitado y no puede usarse en nuevas asociaciones.</li>
 * </ul>
 */
public enum EstadoDepartamento {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String descripcion;

    EstadoDepartamento(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Retorna la descripción legible del estado.
     *
     * @return descripción del estado
     */
    public String getDescripcion() {
        return descripcion;
    }
}
