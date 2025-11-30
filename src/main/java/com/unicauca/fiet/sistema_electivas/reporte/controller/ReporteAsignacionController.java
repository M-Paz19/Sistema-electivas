package com.unicauca.fiet.sistema_electivas.reporte.controller;

import com.unicauca.fiet.sistema_electivas.reporte.dto.ReporteArchivoResponse;
import com.unicauca.fiet.sistema_electivas.reporte.service.ReporteAsignacionService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Controlador encargado de gestionar la generación y descarga de reportes
 * relacionados con el proceso de asignación de electivas.
 *
 * <p>Incluye los endpoints para generar:</p>
 * <ul>
 *     <li>El reporte técnico detallado (ranking + listas por departamento).</li>
 *     <li>El reporte público para publicación oficial.</li>
 * </ul>
 *
 * <p>Ambos reportes se producen a partir del estado del período académico y su
 * información asociada.</p>
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteAsignacionController {

    private final ReporteAsignacionService reporteAsignacionService;

    /**
     * Genera el reporte técnico detallado para el período indicado y devuelve el
     * archivo Excel resultante como una descarga directa.
     *
     * <p>Este endpoint invoca al método
     * {@link ReporteAsignacionService#generarReporteTecnico(Long)}, el cual:</p>
     *
     * <ul>
     *     <li>Valida el estado del período</li>
     *     <li>Construye el libro Excel con ranking y listas por departamento</li>
     *     <li>Guarda el archivo en el sistema</li>
     *     <li>Actualiza el estado del período</li>
     * </ul>
     *
     * <p>Finalmente, el archivo generado se retorna para descarga inmediata.</p>
     *
     * @param periodoId ID del período académico
     * @return archivo Excel descargable generado dinámicamente
     */
    @GetMapping("/periodos/{periodoId}/reporte-tecnico")
    public ResponseEntity<byte[]> generarReporteTecnico(@PathVariable Long periodoId) {

        Workbook workbook = reporteAsignacionService.generarReporteTecnico(periodoId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            workbook.write(out);

            String fileName = "ReporteTecnico_" + periodoId + ".xlsx";

            return ResponseEntity.ok()
                    .header("Content-Type",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition",
                            "attachment; filename=\"" + fileName + "\"")
                    .body(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel.", e);
        }
    }


    /**
     * Genera el reporte público para el período indicado y devuelve el
     * archivo Excel resultante como una descarga directa.
     *
     * <p>Este endpoint invoca al método
     * {@link ReporteAsignacionService#generarReportePublicacion(Long)}, el cual:</p>
     *
     * <ul>
     *     <li>Valida el estado del período</li>
     *     <li>Construye el libro Excel público con listas por departamento</li>
     *     <li>Guarda el archivo en el sistema</li>
     *     <li>Actualiza el estado del período</li>
     * </ul>
     *
     * <p>Finalmente, el archivo generado se retorna para descarga inmediata.</p>
     *
     * @param periodoId ID del período académico
     * @return archivo Excel descargable generado dinámicamente
     */
    @GetMapping("/periodos/{periodoId}/reporte-publico")
    public ResponseEntity<byte[]> generarReportePublico(
            @PathVariable Long periodoId) {

        Workbook workbook = reporteAsignacionService.generarReportePublicacion(periodoId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            workbook.write(out);

            String fileName = "ReportePublico_" + periodoId + ".xlsx";

            return ResponseEntity.ok()
                    .header("Content-Type",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition",
                            "attachment; filename=\"" + fileName + "\"")
                    .body(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel público.", e);
        }
    }

    /**
     * Descarga el archivo previamente generado del reporte técnico
     * del período académico indicado.
     *
     * @param periodoId ID del período académico
     * @return archivo Excel almacenado para ese período
     */
    @GetMapping("/periodos/{periodoId}/descargar-reporte-tecnico")
    public ResponseEntity<Resource> descargarReporteTecnico(@PathVariable Long periodoId) {

        ReporteArchivoResponse response = reporteAsignacionService.obtenerArchivoReporteTecnico(periodoId);

        return ResponseEntity.ok()
                .header("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition",
                        "attachment; filename=\"" + response.getNombreArchivo() + "\"")
                .body(response.getArchivo());
    }

    /**
     * Descarga el archivo previamente generado del reporte público
     * del período académico indicado.
     *
     * @param periodoId ID del período académico
     * @return archivo Excel almacenado para ese período
     */
    @GetMapping("/periodos/{periodoId}/descargar-reporte-publico")
    public ResponseEntity<Resource> descargarReportePublico(@PathVariable Long periodoId) {

        ReporteArchivoResponse response =
                reporteAsignacionService.obtenerArchivoReportePublico(periodoId);

        return ResponseEntity.ok()
                .header("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition",
                        "attachment; filename=\"" + response.getNombreArchivo() + "\"")
                .body(response.getArchivo());
    }

}
