package com.unicauca.fiet.sistema_electivas.asignacion.dto;

import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO que representa la información de un estudiante dentro del proceso
 * de asignación de una electiva específica.
 *
 * <p>Incluye datos personales básicos, estado de asignación (ASIGNADO o EN_ESPERA),
 * así como la posición numérica del estudiante según los criterios oficiales
 * de priorización definidos por la universidad.</p>
 *
 * <p>Este DTO forma parte de la estructura del reporte de una oferta y permite
 * mostrar de manera ordenada y entendible el resultado final del proceso de
 * asignación de cupos.</p>
 */
@Data
public class EstudianteAsignacionDTO {

    /** Posición del estudiante dentro del orden oficial de la oferta. */
    private int numero;

    /** Código único del estudiante en el sistema académico. */
    private String codigo;

    /** Apellidos del estudiante. */
    private String apellidos;

    /** Nombres del estudiante. */
    private String nombres;

    /** Correo institucional o usuario académico del estudiante. */
    private String usuario;

    /** Indica si el estudiante pertenece a un programa nivelado. */
    private boolean esNivelado;

    /** Porcentaje oficial de avance en su plan de estudios. */
    private BigDecimal porcentajeAvance;

    /** Estado de la asignación: ASIGNADO o EN_ESPERA. */
    private EstadoAsignacion estado;
}
