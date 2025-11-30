package com.unicauca.fiet.sistema_electivas.reporte.dto;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteAsignacionReporteResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.DatosAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioResponse;
import lombok.Data;

import java.util.List;

/**
 * DTO que representa el historial de un estudiante en un período académico específico.
 *
 * <p>Incluye:</p>
 * <ul>
 *     <li>El período académico.</li>
 *     <li>Datos académicos del estudiante en ese período ({@link DatosAcademicoResponse}).</li>
 *     <li>Lista de respuestas de formularios enviadas por el estudiante ({@link RespuestaFormularioResponse}).</li>
 *     <li>Lista de asignaciones de electivas ({@link EstudianteAsignacionReporteResponse.AsignacionElectivaInfo}).</li>
 *     <li>Totales de asignaciones con estado ASIGNADA y LISTA_ESPERA.</li>
 * </ul>
 *
 * <p>Se utiliza para mostrar la trayectoria académica y las asignaciones de electivas
 * de un estudiante por período, consolidando información académica y de formularios.</p>
 */
@Data
public class HistorialEstudiantePeriodoResponse {
    private String periodo;
    private DatosAcademicoResponse datosAcademicos;
    private List<RespuestaFormularioResponse> respuestas;
    private List<EstudianteAsignacionReporteResponse.AsignacionElectivaInfo> asignaciones;

    private Integer totalAsignadas = 0;
    private Integer totalListaEspera = 0;
}
