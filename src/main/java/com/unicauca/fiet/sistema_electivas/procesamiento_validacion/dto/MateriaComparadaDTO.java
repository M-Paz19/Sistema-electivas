package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.Data;
/**
 * Representa la comparación de una materia del plan de estudios con el historial académico.
 *
 * <p>Indica si la materia fue encontrada, aprobada, su semestre correspondiente
 * y observaciones adicionales (por ejemplo: “Pendiente cursar”, “Aprobada (Fish)”, etc.).</p>
 */
@Data
public class MateriaComparadaDTO {
    private String nombre;
    private int semestre;
    private boolean obligatoria;
    private boolean aprobada;
    private Double nota;
    private String observacion; // "Aprobada", "No encontrada", "Nombre similar", etc.
}
