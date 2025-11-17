package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.Data;

/**
 * Representa una materia aprobada leída desde el Excel de historial académico.
 */
@Data
public class MateriaVistaExcel {
    private String periodo;
    private String nombre;
    private Integer creditos;
    private Integer semestre;
    private String nota;
    private String habilitacion;
    private Double definitiva;
    private boolean aprobadaPorLetra; // true si la definitiva viene como "A"
    private String tipo; // puede ser null o vacío
}
