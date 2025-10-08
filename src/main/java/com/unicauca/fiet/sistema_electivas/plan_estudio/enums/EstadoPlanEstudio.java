package com.unicauca.fiet.sistema_electivas.plan_estudio.enums;

/**
 * Enum que representa los posibles estados de un Plan de Estudio.
 *
 * CONFIGURACION_PENDIENTE: Estado inicial, indica que el plan fue creado
 *   pero aún requiere completar su configuración.
 * ACTIVO: El plan está en uso dentro del programa.
 * INACTIVO: El plan está registrado pero no está en uso.
 */
public enum EstadoPlanEstudio {
    CONFIGURACION_PENDIENTE("Configuración pendiente"),
    ACTIVO("Activo"),
    INACTIVO("Inactivo");
    private final String descripcion;

    EstadoPlanEstudio(String descripcion) {
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

