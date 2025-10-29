package com.unicauca.fiet.sistema_electivas.periodo_academico.enums;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;

/**
 * Enum que representa los posibles estados de una {@link Oferta}.
 *
 * <ul>
 *   <li><b>OFERTADA:</b> La electiva está disponible para que los estudiantes se inscriban en el periodo académico actual.</li>
 *   <li><b>EN_CURSO:</b> La electiva ya inició y está en desarrollo durante el periodo académico.</li>
 *   <li><b>CERRADA:</b> La electiva ha finalizado; se conserva su historial pero no puede ser modificada ni reabierta.</li>
 * </ul>
 */
public enum EstadoOferta {
    OFERTADA("Ofertada"),
    EN_CURSO("En curso"),
    CERRADA("Cerrada");

    private final String descripcion;

    EstadoOferta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
