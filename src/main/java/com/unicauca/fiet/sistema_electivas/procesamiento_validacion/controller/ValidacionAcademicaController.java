package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.controller;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.*;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service.ValidacionAcademicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Gestiona la carga de datos SIMCA, el manejo de inconsistencias
 * y las validaciones de aptitud (nivelados, avance, etc.).
 */
@RestController
@RequestMapping("/api/v1/validacion")
@RequiredArgsConstructor
public class ValidacionAcademicaController {

    private final ValidacionAcademicaService validacionService;

    /**
     * HU 2.1.1: Carga uno o más archivos CSV con datos de SIMCA.
     * Procesa los archivos, crea las entidades DatosAcademico y reporta inconsistencias.
     *
     * @param periodoId ID del período académico.
     * @param archivos Lista de archivos (MultipartFile) subidos.
     * @return ResponseEntity con SimcaCargaResponse (resumen e inconsistencias).
     */
    @PostMapping("/periodos/{periodoId}/cargar-simca")
    public ResponseEntity<SimcaCargaResponse> cargarDatosSimca(
            @PathVariable Long periodoId,
            @RequestParam("archivos") List<MultipartFile> archivos) {

        if (archivos == null || archivos.isEmpty() || archivos.get(0).isEmpty()) {
            throw new BusinessException("No se proporcionaron archivos para cargar.");
        }

        SimcaCargaResponse response = validacionService.cargarYValidarDatosSimca(periodoId, archivos);
        return ResponseEntity.ok(response);
    }

    /**
     * HU 2.1.2: Obtiene la lista de estudiantes con inconsistencias (Código no coincide).
     *
     * @param periodoId ID del período académico.
     * @return Lista de DTO de respuestas inconsistentes.
     */
    @GetMapping("/periodos/{periodoId}/inconsistencias")
    public ResponseEntity<List<RespuestaFormularioResponse>> listarInconsistencias(@PathVariable Long periodoId) {
        List<RespuestaFormularioResponse> inconsistencias = validacionService.obtenerInconsistencias(periodoId);
        return ResponseEntity.ok(inconsistencias);
    }

    /**
     * HU 2.1.2.1: Corrige el código de estudiante de una respuesta inconsistente.
     *
     * @param respuestaId ID de la RespuestasFormulario a corregir.
     * @param request DTO que contiene el nuevo código.
     * @return La respuesta de formulario con su estado actualizado a CORREGIDO.
     */
    @PostMapping("/respuestas/{respuestaId}/corregir-codigo")
    public ResponseEntity<RespuestaFormularioDesicionResponse> corregirCodigo(
            @PathVariable Long respuestaId,
            @Valid @RequestBody CorregirCodigoRequest request) {

        RespuestaFormularioDesicionResponse response = validacionService.corregirCodigoEstudiante(respuestaId, request.getNuevoCodigo());
        return ResponseEntity.ok(response);
    }

    /**
     * HU 2.1.2.2: Toma la decisión de incluir o descartar una respuesta inconsistente.
     *
     * @param respuestaId ID de la RespuestasFormulario.
     * @param incluir boolean que indica la decisión (true=incluir, false=descartar).
     * @return La respuesta de formulario con su estado actualizado.
     */
    @PostMapping("/respuestas/{respuestaId}/decision-inconsistencia")
    public ResponseEntity<RespuestaFormularioDesicionResponse> decisionSobreInconsistencia(
            @PathVariable Long respuestaId,
            @RequestParam boolean incluir) {

        RespuestaFormularioDesicionResponse response = validacionService.tomarDecisionInconsistencia(respuestaId, incluir);
        return ResponseEntity.ok(response);
    }

    /**
     * HU 2.1.2.3: Regenera un lote TXT con los códigos corregidos/forzados.
     * Devuelve el contenido del archivo TXT para descarga.
     *
     * @param periodoId ID del período académico.
     * @return ResponseEntity que contiene el texto plano del archivo TXT.
     */
    @GetMapping("/periodos/{periodoId}/regenerar-lote-corregidos")
    public ResponseEntity<String> regenerarLoteCorregidos(@PathVariable Long periodoId) {
        String contenidoTxt = validacionService.regenerarLoteCorregidos(periodoId);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Lote_Corregidos.txt");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=utf-8");

        return new ResponseEntity<>(contenidoTxt, headers, HttpStatus.OK);
    }

    /**
     * HU 2.2.1: Preselecciona y devuelve una lista de 'Posibles Nivelados'.
     * Es un POST porque esta acción modifica el estado de los estudiantes
     * en la base de datos (los marca como POSIBLE_NIVELADO).
     *
     * @param periodoId ID del período académico.
     * @return Lista de DTO de los estudiantes marcados como posibles nivelados.
     */
    @PostMapping("/periodos/{periodoId}/preseleccionar-nivelados")
    public ResponseEntity<List<DatosAcademicoResponse>> preseleccionarNivelados(@PathVariable Long periodoId) {
        List<DatosAcademicoResponse> response = validacionService.preseleccionarNivelados(periodoId);
        return ResponseEntity.ok(response);
    }
}