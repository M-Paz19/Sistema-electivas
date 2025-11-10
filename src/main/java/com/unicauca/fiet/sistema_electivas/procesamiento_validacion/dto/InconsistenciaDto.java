package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa una inconsistencia encontrada durante la carga
 * de datos de SIMCA (HU 2.1.1.3 y 2.1.1.4).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InconsistenciaDto {

    /**
     * ID de la RespuestasFormulario (si se logra encontrar).
     * Puede ser null si el código del CSV no está en la lista de preinscritos.
     */
    private Long respuestaId;
    private String codigoEstudianteCsv;
    private String nombreEstudianteCsv;
    private String error;
    private String archivoOrigen;
}