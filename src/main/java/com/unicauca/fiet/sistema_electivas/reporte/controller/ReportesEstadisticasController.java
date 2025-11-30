package com.unicauca.fiet.sistema_electivas.reporte.controller;

import com.unicauca.fiet.sistema_electivas.reporte.dto.*;
import com.unicauca.fiet.sistema_electivas.reporte.service.ReportesEstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reportes/estadisticas")
@RequiredArgsConstructor
public class ReportesEstadisticasController {

    private final ReportesEstadisticasService reportesEstadisticasService;

    /**
     * Obtiene la distribución de cuántas electivas fueron asignadas
     * a cada estudiante en un período académico dado.
     * HU 4.1 – Distribución de electivas asignadas por estudiante.
     *
     * @param periodoId ID del período académico
     * @return distribución en JSON
     */
    @GetMapping("/periodos/{periodoId}/distribucion-asignaciones")
    public ResponseEntity<DistribucionAsignacionesResponse> obtenerDistribucionAsignaciones(
            @PathVariable Long periodoId) {

        DistribucionAsignacionesResponse response =
                reportesEstadisticasService.obtenerDistribucionAsignaciones(periodoId);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene la distribución de asignaciones por programa académico,
     * indicando cuántas electivas fueron asignadas dentro de cada programa
     * para un período académico dado.
     * HU 4.1 – Distribución de electivas asignadas por programa.
     *
     * @param periodoId ID del período académico
     * @return distribución en JSON
     */
    @GetMapping("/periodos/{periodoId}/distribucion-asignaciones-programa")
    public ResponseEntity<DistribucionAsignacionesPorProgramaResponse> obtenerDistribucionAsignacionesPorPrograma(
            @PathVariable Long periodoId) {

        DistribucionAsignacionesPorProgramaResponse response =
                reportesEstadisticasService.obtenerDistribucionPorPrograma(periodoId);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un resumen del procesamiento de un período académico,
     * incluyendo los estados de las respuestas de formulario y de la aptitud
     * de los estudiantes.
     *
     * <p>Se incluyen únicamente los siguientes estados:
     * <ul>
     *     <li>Formulario: DUPLICADO, NO_CUMPLE, DESCARTADO, DESCARTADO_SIMCA, DATOS_CARGADOS</li>
     *     <li>Aptitud: NO_APTO, EXCLUIDO_POR_ELECTIVAS, ASIGNACION_PROCESADA</li>
     * </ul>
     *
     * El resumen consolida la cantidad de estudiantes en cada estado, junto
     * con el título del estado y su descripción.
     *
     * HU X.X – Resumen de procesamiento de un período académico.
     *
     * @param periodoId ID del período académico
     * @return resumen consolidado en formato JSON
     */
    @GetMapping("/periodos/{periodoId}/resumen-procesamiento")
    public ResponseEntity<ResumenProcesamientoPeriodoResponse> obtenerResumenProcesamiento(
            @PathVariable Long periodoId) {

        ResumenProcesamientoPeriodoResponse response =
                reportesEstadisticasService.obtenerResumenProcesamiento(periodoId);

        return ResponseEntity.ok(response);
    }


    /**
     * Genera un reporte consolidado de distribución de asignaciones
     * en Excel para un período académico dado.
     *
     * @param periodoId ID del período académico
     * @return archivo Excel
     */
    @GetMapping("/periodos/{periodoId}/reporte-distribucion")
    public ResponseEntity<byte[]> generarReporteDistribucion(@PathVariable Long periodoId) {
        byte[] excelBytes = reportesEstadisticasService.generarReporteDistribucionExcel(periodoId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("reporte_distribucion.xlsx")
                .build());
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * Obtiene el ranking de popularidad de electivas para un período académico,
     * calculando cuántas veces fue seleccionada cada electiva por los estudiantes
     * y distribuyendo los conteos por número de opción (1ra, 2da, 3ra, etc.).
     *
     * <p>HU 4.2 – Popularidad de electivas seleccionadas por los estudiantes.</p>
     *
     * @param periodoId ID del período académico.
     * @return listado de electivas con su conteo total y distribución por opción.
     */
    @GetMapping("/periodos/{periodoId}/popularidad-electivas")
    public ResponseEntity<PopularidadElectivasResponse> obtenerPopularidadElectivas(
            @PathVariable Long periodoId) {

        PopularidadElectivasResponse response =
                reportesEstadisticasService.obtenerPopularidad(periodoId);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el ranking de popularidad de electivas para un período académico,
     * incluyendo estudiantes cuyas respuestas fueron descartadas o validadas con
     * observaciones (NO_CUMPLE, DESCARTADO, DESCARTADO_SIMCA, DATOS_CARGADOS).
     *
     * <p>Este reporte permite analizar la demanda total de electivas, incluso de
     * aquellos estudiantes cuyas respuestas no participaron en el proceso final
     * de asignación.</p>
     *
     * <p>HU 4.3 – Popularidad de electivas incluyendo respuestas descartadas.</p>
     *
     * @param periodoId ID del período académico.
     * @return distribución de popularidad considerando todos los estados permitidos.
     */
    @GetMapping("/periodos/{periodoId}/popularidad-electivas-incluyendo-descartados")
    public ResponseEntity<PopularidadElectivasResponse> obtenerPopularidadElectivasIncluyendoDescartados(
            @PathVariable Long periodoId) {

        PopularidadElectivasResponse response =
                reportesEstadisticasService.obtenerPopularidadIncluyendoDescartados(periodoId);

        return ResponseEntity.ok(response);
    }

    /**
     * Genera un reporte de popularidad de electivas
     * (aptos e incluyendo descartados) para un periodo académico.
     *
     * @param periodoId ID del período académico
     * @return archivo Excel
     */
    @GetMapping("/periodos/{periodoId}/reporte-popularidad")
    public ResponseEntity<byte[]> generarReportePopularidad(@PathVariable Long periodoId) {
        byte[] excelBytes = reportesEstadisticasService.generarReportePopularidadExcel(periodoId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("reporte_popularidad.xlsx")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }


}