package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums;

public enum EstadoAptitud {
    /**
     * Los datos fueron cargados desde SIMCA, pero no se ha ejecutado ninguna validación.
     * (Estado inicial post-HU 2.1.1)
     */
    PENDIENTE_VALIDACION,

    /**
     * Marcado como 'Posible Nivelado' por la HU 2.2.1,
     * pendiente de verificación manual con historia académica.
     */
    POSIBLE_NIVELADO,

    /**
     * Se verificó manualmente y se confirmó que ES NIVELADO.
     * (Estado final de HU 2.3.1)
     */
    NIVELADO_CONFIRMADO,

    /**
     * Se verificó manualmente y se confirmó que NO ES NIVELADO.
     * (Estado final de HU 2.3.1)
     */
    NIVELADO_DESCARTADO,

    /**
     * Se calculó su porcentaje de avance (HU 2.4.1).
     * Este estado es para los NO nivelados.
     */
    AVANCE_CALCULADO,

    /**
     * Cumple todos los requisitos (Avance > 65% Y Semestres >= 7) O (Es Nivelado).
     * (Estado final HU 2.5.1)
     */
    APTO,

    /**
     * No cumple algún requisito.
     * (Estado final HU 2.5.1)
     */
    NO_APTO
}
