package com.unicauca.fiet.sistema_electivas.electiva.enums;

import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;

/**
 * Enum que representa los posibles estados de una {@link Electiva}.
 *
 * <ul>
 *   <li><b>BORRADOR:</b> La electiva está en etapa de diseño o revisión y aún no ha sido aprobada.</li>
 *   <li><b>APROBADA:</b> La electiva fue aprobada por el comité o autoridad correspondiente y puede ofertarse.</li>
 *   <li><b>INACTIVA:</b> La electiva fue deshabilitada temporal o permanentemente, y no puede ofertarse ni modificarse.</li>
 * </ul>
 */
public enum EstadoElectiva {
    BORRADOR("Borrador"),
    APROBADA("Aprobada"),
    INACTIVA("Inactiva");

    private final String descripcion;

    EstadoElectiva(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Retorna una descripción legible del estado.
     *
     * @return descripción del estado
     */
    public String getDescripcion() {
        return descripcion;
    }
}
