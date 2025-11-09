package com.unicauca.fiet.sistema_electivas.archivo.controller;

import com.unicauca.fiet.sistema_electivas.archivo.dto.ArchivoResponse;
import com.unicauca.fiet.sistema_electivas.archivo.service.ArchivoConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;

import java.nio.file.Paths;
import java.util.List;

/**
 * Controlador REST que permite consultar los archivos de lotes
 * generados para SIMCA, asociados a un período académico.
 */
@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
public class ArchivoConsultaController {

    private final ArchivoConsultaService archivoConsultaService;

    /**
     * Endpoint para obtener los archivos de tipo LOTES_CODIGOS asociados a un período.
     *
     * @param idPeriodo identificador del período académico
     * @return lista de archivos con su información básica
     */
    @GetMapping("/lotes/{idPeriodo}")
    public ResponseEntity<List<ArchivoResponse>> obtenerLotesPorPeriodo(@PathVariable Long idPeriodo) {
        List<ArchivoResponse> archivos = archivoConsultaService.obtenerLotesCodigosPorPeriodo(idPeriodo);
        return ResponseEntity.ok(archivos);
    }
    /**
     * Descarga un archivo individual a partir de su ID.
     *
     * @param idArchivo identificador del archivo a descargar
     * @return archivo como recurso binario listo para descarga
     */
    @GetMapping("/descargar/{idArchivo}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long idArchivo) {
        Resource recurso = archivoConsultaService.descargarArchivo(idArchivo);

        // Determinar nombre original del archivo
        String nombreArchivo = Paths.get(recurso.getFilename()).getFileName().toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(recurso);
    }
    /**
     * Descarga todos los archivos de tipo LOTES_CODIGOS asociados a un período en un solo ZIP.
     *
     * @param idPeriodo identificador del período académico
     * @return archivo ZIP conteniendo todos los lotes del período
     */
    @GetMapping("/descargar/lotes/{idPeriodo}")
    public ResponseEntity<Resource> descargarLotesZip(@PathVariable Long idPeriodo) {
        Resource recursoZip = archivoConsultaService.descargarLotesZip(idPeriodo);

        String nombreZip = "Lotes_SIMCA_Periodo_" + idPeriodo + ".zip";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreZip + "\"")
                .body(recursoZip);
    }
}