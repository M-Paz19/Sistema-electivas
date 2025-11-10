package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.Data;
/**
 * Representa la respuesta final al registrar la decisión sobre el estado de nivelación
 * de un estudiante en el sistema.
 *
 * <p>Incluye los datos académicos básicos y el estado final asignado
 * (por ejemplo: "NIVELADO" o "NO_NIVELADO").</p>
 */
@Data
public class ValidacionNiveladoResponseDTO {
    private Long idDatosAcademicos;
    private String codigoEstudiante;
    private String nombreCompleto;
    private String programa;
    private String estado;
}
