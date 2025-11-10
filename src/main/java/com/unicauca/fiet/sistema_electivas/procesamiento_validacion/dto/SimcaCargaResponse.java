package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO para la respuesta de la carga de archivos SIMCA (HU 2.1.1).
 * Contiene el mensaje de resumen y el detalle de inconsistencias.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimcaCargaResponse {

    /** Mensaje de resumen (Ej: "Datos de SIMCA cargados exitosamente...") */
    private String mensaje;

    /** Cantidad de archivos que fueron procesados. */
    private int archivosProcesados;

    /** Total de registros de estudiantes que se guardaron en la BD. */
    private int registrosCargadosExitosamente;

    /** Total de registros en los archivos que no coincidieron. */
    private int inconsistenciasEncontradas;

    /** Lista detallada de las inconsistencias encontradas (HU 2.1.1.4). */
    private List<InconsistenciaDto> detalleInconsistencias;
}