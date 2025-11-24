package com.unicauca.fiet.sistema_electivas.asignacion.service;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;

public interface ReglasElectivasService {
    /**
     * Determina cuántas electivas debe intentar asignar el sistema a un estudiante,
     * aplicando las reglas oficiales del proceso de asignación.
     *
     * <p>Este método consolida los 4 casos posibles:
     * <ul>
     *     <li><b>Nivelados →</b> reciben exactamente las electivas definidas para su semestre.</li>
     *     <li><b>No nivelados con avance 100% →</b> reciben todas las que les faltan.</li>
     *     <li><b>No nivelados con avance > 98%</b> → reciben también todas las que les faltan.</li>
     *     <li><b>No nivelados avance < 98%</b> → se calcula según créditos obligatorios restantes.</li>
     * </ul>
     * </p>
     *
     * @param dato entidad {@link DatosAcademico} del estudiante en proceso
     * @return cantidad de electivas que se deben intentar asignar
     */
    int calcularCantidadElectivasAAsignar(DatosAcademico dato);
}
