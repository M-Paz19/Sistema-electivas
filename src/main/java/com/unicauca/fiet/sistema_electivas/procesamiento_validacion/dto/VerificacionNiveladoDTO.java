package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
/**
 * Representa el resultado de la verificación del estado de nivelación de un estudiante.
 *
 * <p>Contiene información general del estudiante, el porcentaje de avance académico,
 * si cumple o no con los requisitos de nivelación y el detalle comparativo de materias
 * esperadas frente a las cursadas.</p>
 */
@Data
public class VerificacionNiveladoDTO {
    private String codigoEstudiante;
    private String nombre;
    private String programa;

    private boolean nivelado;
    private Integer semestreVerificado;
    private String mensajeResumen;
    private List<MateriaComparadaDTO> comparacionMaterias;
}
