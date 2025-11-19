package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums;

/**
 * Enum que representa los posibles estados de aptitud de un estudiante
 * durante el proceso de validación académica y cálculo de avance.
 *
 * <ul>
 *   <li><b>PENDIENTE_VALIDACION:</b> Los datos fueron cargados desde SIMCA, pero aún no se ha ejecutado ninguna validación inicial.</li>
 *   <li><b>POSIBLE_NIVELADO:</b> El estudiante fue marcado como posible nivelado y requiere verificación manual de su historia académica.</li>
 *   <li><b>NIVELADO_CONFIRMADO:</b> Se confirmó manualmente que el estudiante sí es nivelado.</li>
 *   <li><b>NIVELADO_DESCARTADO:</b> Se confirmó manualmente que el estudiante no es nivelado.</li>
 *   <li><b>AVANCE_CALCULADO:</b> Se calculó el porcentaje de avance para los estudiantes no nivelados.</li>
 *   <li><b>APTO:</b> El estudiante cumple los requisitos (Avance > 65% y Semestres ≥ 7) o ha sido marcado como nivelado.</li>
 *   <li><b>NO_APTO:</b> El estudiante no cumple los requisitos establecidos para ser apto.</li>
 *   <li><b>EXCLUIDO_POR_ELECTIVAS:</b> El estudiante ya cursó todas las electivas y no puede postular a más.</li>
 * </ul>
 */
public enum EstadoAptitud {

    PENDIENTE_VALIDACION("Datos cargados, pendiente de validación"),
    POSIBLE_NIVELADO("Posible nivelado, pendiente de verificación manual"),
    NIVELADO_CONFIRMADO("Nivelado confirmado"),
    NIVELADO_DESCARTADO("Nivelado descartado"),
    AVANCE_CALCULADO("Porcentaje de avance calculado"),
    APTO("Cumple los requisitos de aptitud"),
    NO_APTO("No cumple los requisitos de aptitud"),
    EXCLUIDO_POR_ELECTIVAS("El estudiante ya cursó todas las electivas");

    private final String descripcion;

    EstadoAptitud(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

