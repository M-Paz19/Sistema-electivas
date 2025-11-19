package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.controller;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.*;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
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
@RequestMapping("/api/validacion-academica")
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
            @RequestParam("archivos") MultipartFile[] archivos) {

        if (archivos == null || archivos.length == 0 || archivos[0].isEmpty()) {
            throw new BusinessException("No se proporcionaron archivos para cargar.");
        }

        SimcaCargaResponse response = validacionService.cargarYValidarDatosSimca(periodoId, archivos);
        return ResponseEntity.ok(response);
    }
    /**
     * Endpoint que permite consultar los datos académicos asociados a un período académico.
     *
     * <p>La consulta puede retornar todos los registros o aplicar un filtro opcional por uno o
     * varios estados de aptitud. Esto permite obtener información específica como estudiantes
     * marcados como PENDIENTE_VALIDACION, POSIBLE_NIVELADO, NO_APTO, entre otros.</p>
     *
     * @param periodoId identificador del período académico
     * @param estados lista opcional de estados de aptitud a filtrar; si es {@code null} o está vacía,
     *                se retornan todos los datos académicos del período
     * @return lista de {@link DatosAcademicoResponse} correspondiente a los estudiantes del período
     */
    @GetMapping("/periodo/{periodoId}/datos-academicos")
    public ResponseEntity<List<DatosAcademicoResponse>> obtenerDatosPorPeriodo(
            @PathVariable Long periodoId,
            @RequestParam(required = false) List<EstadoAptitud> estados
    ) {
        List<DatosAcademicoResponse> respuesta =
                validacionService.obtenerDatosAcademicosPorPeriodo(periodoId, estados);

        return ResponseEntity.ok(respuesta);
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
     * HU 2.1.2.2: Toma la decisión de incluir o descartar una respuesta inconsistente.
     *
     * @param respuestaId ID de la RespuestasFormulario.
     * @param incluir boolean que indica la decisión (true=incluir, false=descartar).
     * @param request DTO que contiene el nuevo código.
     * @return La respuesta de formulario con su estado actualizado.
     */
    @PostMapping("/respuestas/{respuestaId}/decision-inconsistencia")
    public ResponseEntity<RespuestaFormularioDesicionResponse> decisionSobreInconsistencia(
            @PathVariable Long respuestaId,
            @RequestParam boolean incluir,
            @Valid @RequestBody CorregirCodigoRequest request) {

        RespuestaFormularioDesicionResponse response = validacionService.resolverInconsistenciaSimca(respuestaId, incluir,request.getNuevoCodigo());
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
     * HU 2.3.1: Calcula el porcentaje de avance académico de todos los estudiantes de un período.
     *
     * <p>Este proceso se ejecuta únicamente si el período académico está en estado
     * {@code PROCESO_CALCULO_AVANCE}. Antes de calcular, el sistema verifica que no
     * existan registros con estado {@code POSIBLE_NIVELADO}.</p>
     *
     * <p>El cálculo asigna:
     * <ul>
     *   <li>100% de avance a los estudiantes nivelados.</li>
     *   <li>Un porcentaje proporcional según los créditos aprobados frente al plan de estudios
     *       para los demás estudiantes.</li>
     * </ul>
     * Finalmente, el período cambia de estado a {@code PROCESO_CALCULO_APTITUD}.</p>
     *
     * @param periodoId ID del período académico.
     * @return ResponseEntity con {@link CambioEstadoValidacionResponse} que resume el resultado del proceso.
     * @throws ResourceNotFoundException si el período o los datos académicos no existen.
     * @throws InvalidStateException si el período no está en estado {@code PROCESO_CALCULO_AVANCE}.
     */
    @PostMapping("/periodos/{periodoId}/calcular-porcentaje-avance")
    public ResponseEntity<CambioEstadoValidacionResponse> calcularPorcentajeAvance(@PathVariable Long periodoId) {
        CambioEstadoValidacionResponse response = validacionService.calcularPorcentajeAvance(periodoId);
        return ResponseEntity.ok(response);
    }

    /**
     * HU 2.5.1: Valida automáticamente los requisitos académicos generales
     * (porcentaje de avance y semestres cursados) para todos los estudiantes
     * de un período académico.
     *
     * <p>Este proceso solo puede ejecutarse cuando el período está en estado
     * {@code PROCESO_CALCULO_APTITUD}. Para cada estudiante:</p>
     *
     * <ul>
     *   <li>Si es nivelado → APTO automáticamente.</li>
     *   <li>Si no es nivelado:
     *       <ul>
     *         <li>Debe cumplir avance >= 65%</li>
     *       </ul>
     *   </li>
     *   <li>Si no cumple las condiciones → NO_APTO.</li>
     * </ul>
     *
     *
     * @param periodoId ID del período académico.
     * @return ResponseEntity con un resumen del proceso.
     */
    @PostMapping("/periodos/{periodoId}/validar-requisitos-generales")
    public ResponseEntity<CambioEstadoValidacionResponse> validarRequisitosGenerales(
            @PathVariable Long periodoId) {

        CambioEstadoValidacionResponse response =
                validacionService.validarRequisitosGenerales(periodoId);

        return ResponseEntity.ok(response);
    }

}