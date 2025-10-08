package com.unicauca.fiet.sistema_electivas.plan_estudio.enums;

/**
 * Enum que representa los tipos posibles de una materia dentro del plan de estudios.
 *
 * <p>Estos valores determinan la naturaleza académica de la asignatura:
 * <ul>
 *   <li><b>OBLIGATORIA</b>: Materia que el estudiante debe cursar obligatoriamente.</li>
 *   <li><b>ELECTIVA</b>: Materia que el estudiante puede elegir dentro de un conjunto de opciones.</li>
 *   <li><b>TRABAJO_GRADO</b>: Materia asociada al trabajo de grado o tesis final.</li>
 * </ul>
 */
public enum TipoMateria {

    OBLIGATORIA("Obligatoria"),
    ELECTIVA("Electiva"),
    TRABAJO_GRADO("Trabajo de grado");

    private final String descripcion;

    TipoMateria(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Retorna una descripción legible del tipo de materia.
     *
     * @return descripción textual del tipo
     */
    public String getDescripcion() {
        return descripcion;
    }
}
