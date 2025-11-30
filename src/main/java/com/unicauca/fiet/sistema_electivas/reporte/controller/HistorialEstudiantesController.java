package com.unicauca.fiet.sistema_electivas.reporte.controller;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import com.unicauca.fiet.sistema_electivas.reporte.dto.EstudianteBusquedaResponse;
import com.unicauca.fiet.sistema_electivas.reporte.dto.HistorialEstudiantePeriodoResponse;
import com.unicauca.fiet.sistema_electivas.reporte.service.HistorialEstudiantesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes/historial")
@RequiredArgsConstructor
public class HistorialEstudiantesController {

    private final HistorialEstudiantesService historialEstudiantesService;

    /**
     * Consulta el historial completo de un estudiante agrupado por período.
     *
     * <p>Incluye:</p>
     * <ul>
     *   <li>Datos académicos del estudiante por período</li>
     *   <li>Respuestas de formulario</li>
     *   <li>Asignaciones de electivas</li>
     *   <li>Totales por estado de asignación (ASIGNADA, LISTA_ESPERA)</li>
     * </ul>
     *
     * <p>HU 4.4 – Consulta de historial de asignación por estudiante.</p>
     *
     * @param codigoEstudiante código del estudiante.
     * @return historial consolidado por período.
     */
    @GetMapping("/estudiantes/{codigoEstudiante}")
    public ResponseEntity<List<HistorialEstudiantePeriodoResponse>> obtenerHistorialPorEstudiante(
            @PathVariable String codigoEstudiante) {

        List<HistorialEstudiantePeriodoResponse> historial =
                historialEstudiantesService.obtenerHistorialPorEstudiante(codigoEstudiante);

        return ResponseEntity.ok(historial);
    }

    /**
     * Endpoint para buscar estudiantes por código, nombre o apellido.
     *
     * <p>Realiza una búsqueda en {@link DatosAcademico} y {@link RespuestasFormulario},
     * combina los resultados y elimina duplicados por {@code codigoEstudiante}.</p>
     *
     * <p>Devuelve información mínima de cada estudiante coincidente.</p>
     *
     * @param filtro texto a buscar en código, nombres o apellidos
     * @return {@link ResponseEntity} con lista de {@link EstudianteBusquedaResponse}
     */
    @GetMapping("/estudiantes")
    public ResponseEntity<List<EstudianteBusquedaResponse>> buscar(@RequestParam String filtro) {
        return ResponseEntity.ok(historialEstudiantesService.buscar(filtro));
    }
}
