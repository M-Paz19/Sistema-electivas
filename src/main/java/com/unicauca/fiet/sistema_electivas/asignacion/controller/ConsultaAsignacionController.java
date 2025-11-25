package com.unicauca.fiet.sistema_electivas.asignacion.controller;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.DepartamentoReporteDTO;
import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteAsignacionReporteResponse;
import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteOrdenamientoResponse;
import com.unicauca.fiet.sistema_electivas.asignacion.service.ConsultaAsignacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador encargado de exponer endpoints de consulta
 * relacionados con estudiantes aptos y su ordenamiento
 * para el proceso de asignación de electivas.
 *
 * <p>Este controlador no modifica estados ni realiza asignaciones,
 * solo permite consultar la información existente.</p>
 */
@RestController
@RequestMapping("/api/consulta-asignacion")
@RequiredArgsConstructor
public class ConsultaAsignacionController {

    private final ConsultaAsignacionService consultaAsignacionService;

    /**
     * Obtiene la lista de estudiantes aptos ordenados para un período académico.
     *
     * <p>Este endpoint devuelve únicamente información de consulta, sin modificar
     * ningún estado del sistema.</p>
     *
     * <p>Los estudiantes se ordenan aplicando los criterios oficiales:</p>
     * <ul>
     *   <li>Porcentaje de avance (DESC)</li>
     *   <li>Promedio de carrera (DESC)</li>
     *   <li>Electivas faltantes (ASC)</li>
     * </ul>
     *
     * @param periodoId ID del período académico
     * @return Lista de estudiantes aptos ordenados para asignación
     */
    @GetMapping("/periodos/{periodoId}/aptos/ordenados")
    public ResponseEntity<List<EstudianteOrdenamientoResponse>> obtenerAptosOrdenados(
            @PathVariable Long periodoId
    ) {
        List<EstudianteOrdenamientoResponse> estudiantes =
                consultaAsignacionService.obtenerAptosOrdenados(periodoId);

        return ResponseEntity.ok(estudiantes);
    }

    /**
     * Genera la estructura del reporte de listas de asignación
     * para todas las ofertas del período académico.
     *
     * <p>Este endpoint permite consultar la información procesada
     * posteriormente utilizada para construir el reporte PDF o Excel.</p>
     *
     * <p>Incluye por cada departamento:</p>
     * <ul>
     *   <li>Electivas del período</li>
     *   <li>Programas que pueden cursar cada electiva</li>
     *   <li>Listas de estudiantes asignados y en lista de espera</li>
     *   <li>Ordenamiento oficial aplicado</li>
     * </ul>
     *
     * @param periodoId ID del período académico, el cual debe estar en estado ASIGNACION_PROCESADA
     * @return Estructura jerárquica de departamentos, ofertas y estudiantes
     */
    @GetMapping("/periodos/{periodoId}/reporte/listasAsignacion/tecnica")
    public ResponseEntity<List<DepartamentoReporteDTO>> generarReporteListaDeAsignacionTecnica(
            @PathVariable Long periodoId
    ) {
        List<DepartamentoReporteDTO> reporte =
                consultaAsignacionService.generarListasDeAsigancionPorDepartamentos(periodoId);

        return ResponseEntity.ok(reporte);
    }

    /**
     * Genera el reporte detallado del ranking de asignación por estudiante.
     *
     * <p>Incluye, para cada estudiante apto del período:</p>
     * <ul>
     *     <li>Datos académicos</li>
     *     <li>Electivas asignadas y en lista de espera</li>
     *     <li>Programas asociados a cada electiva</li>
     *     <li>Métricas de avance y promedio</li>
     * </ul>
     *
     * <p>Este reporte solo puede generarse cuando el período se
     * encuentra en estado ASIGNACION_PROCESADA.</p>
     *
     * @param periodoId ID del período académico
     * @return Lista de estudiantes con su información completa de asignación
     */
    @GetMapping("/periodos/{periodoId}/reporte/ranking")
    public ResponseEntity<List<EstudianteAsignacionReporteResponse>> generarReporteRanking(
            @PathVariable Long periodoId
    ) {
        List<EstudianteAsignacionReporteResponse> reporte =
                consultaAsignacionService.generarReporteRanking(periodoId);

        return ResponseEntity.ok(reporte);
    }
}
