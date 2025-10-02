package com.unicauca.fiet.sistema_electivas.enums;

/**
 * Enumeración que representa los posibles estados de un {@code Programa}.
 * <p>
 * Define las diferentes fases del ciclo de vida de un programa académico,
 * permitiendo controlar su disponibilidad en el sistema.
 */
public enum EstadoPrograma {
    PENDIENTE_PLAN("Pendiente de plan"),
    APROBADO("Aprobado"),
    DESHABILITADO("Deshabilitado");


    private final String descripcion;

    EstadoPrograma(String descripcion) {
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