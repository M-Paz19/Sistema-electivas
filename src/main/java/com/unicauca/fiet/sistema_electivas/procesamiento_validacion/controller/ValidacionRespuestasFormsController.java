package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.controller;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CambioEstadoValidacionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CorregirCodigoRequest;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioDesicionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service.ValidacionRespuestasFormsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la consulta y gestión de respuestas de formularios
 * asociadas a los períodos académicos.
 *
 * <p>Permite obtener las respuestas importadas desde Google Forms
 * como paso previo al procesamiento o validación académica.</p>
 */
@RestController
@RequestMapping("/api/procesamiento")
@RequiredArgsConstructor
public class ValidacionRespuestasFormsController {

    private final ValidacionRespuestasFormsServiceImpl procesamientoService;


    /**
     * Obtiene todas las respuestas asociadas a un período académico específico.
     *
     * @param periodoId ID del período académico
     * @return Lista de respuestas del formulario vinculadas a ese período.
     */
    @GetMapping("/periodos/{periodoId}/respuestas")
    public ResponseEntity<List<RespuestaFormularioResponse>> listarRespuestasPorPeriodo(@PathVariable Long periodoId) {
        List<RespuestaFormularioResponse> respuestas = procesamientoService.obtenerRespuestasPorPeriodo(periodoId);
        return ResponseEntity.ok(respuestas);
    }

    /**
     * Aplica el proceso de filtrado de duplicados para un período académico.
     *
     * <p>El sistema conserva solo la primera respuesta (por timestamp más antiguo)
     * de cada estudiante y elimina las demás. Luego cambia el estado del período
     * a <b>PROCESO_FILTRADO_DUPLICADOS</b>.</p>
     *
     * @param periodoId ID del período académico sobre el cual aplicar el filtro
     * @return {@link CambioEstadoValidacionResponse} con información del nuevo estado
     *         y un mensaje resumen del proceso ejecutado.
     */
    @PostMapping("/periodos/{periodoId}/filtro-duplicados")
    public ResponseEntity<CambioEstadoValidacionResponse> aplicarFiltroDuplicados(
            @PathVariable Long periodoId
    ) {
        CambioEstadoValidacionResponse response = procesamientoService.aplicarFiltroDuplicados(periodoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Aplica el proceso de clasificación por antigüedad para un período académico.
     *
     * <p>Evalúa el código de cada estudiante para determinar su antigüedad.
     * Aquellos con 6 o más semestres son marcados como <b>CUMPLE</b>,
     * los demás como <b>NO_CUMPLE</b>, y los códigos inválidos se marcan
     * como <b>FORMATO_INVALIDO</b>.</p>
     *
     * <p>El estado del período cambia a <b>PROCESO_CLASIFICACION_ANTIGUEDAD</b>.</p>
     *
     * @param periodoId ID del período académico sobre el cual aplicar el filtro
     * @return {@link CambioEstadoValidacionResponse} con información del nuevo estado
     *         y un mensaje resumen del proceso ejecutado.
     */
    @PostMapping("/periodos/{periodoId}/filtro-antiguedad")
    public ResponseEntity<CambioEstadoValidacionResponse> aplicarFiltroAntiguedad(
            @PathVariable Long periodoId
    ) {
        CambioEstadoValidacionResponse response =
                procesamientoService.aplicarFiltroCodigosPorAntiguedad(periodoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Permite revisar manualmente una respuesta con código de estudiante desconocido.
     *
     * <p>El administrador puede decidir si incluir o descartar un estudiante
     * que tiene un formato de código inválido. Esta acción solo se permite
     * cuando el registro está en estado <b>FORMATO_INVALIDO</b>.</p>
     *
     * @param respuestaId ID del registro de respuesta.
     * @param incluir true si se desea incluir al estudiante, false para descartarlo.
     * @return {@link RespuestaFormularioDesicionResponse} con el estado actualizado.
     */
    @PostMapping("/respuestas/{respuestaId}/revision-manual")
    public ResponseEntity<RespuestaFormularioDesicionResponse> revisarManualFormatoInvalido(
            @PathVariable Long respuestaId,
            @RequestParam boolean incluir,
            @Valid @RequestBody CorregirCodigoRequest request
    ) {
        RespuestaFormularioDesicionResponse response =
                procesamientoService.revisarManualFormatoInvalido(respuestaId, incluir, request.getNuevoCodigo());

        return ResponseEntity.ok(response);
    }

    /**
     * Confirma la lista final de estudiantes para la fase de validación con SIMCA.
     *
     * <p>Este proceso valida que no existan registros pendientes de revisión manual
     * (estado {@code FORMATO_INVALIDO}). Si todos fueron revisados, el período pasa
     * al estado <b>PROCESO_CONFIRMACION_SIMCA</b>.</p>
     *
     * @param periodoId ID del período académico.
     * @return {@link CambioEstadoValidacionResponse} con el nuevo estado y mensaje del proceso.
     */
    @PostMapping("/periodos/{periodoId}/confirmar-simca")
    public ResponseEntity<CambioEstadoValidacionResponse> confirmarListaParaSimca(
            @PathVariable Long periodoId
    ) {
        CambioEstadoValidacionResponse response =
                procesamientoService.confirmarListaParaSimca(periodoId);
        return ResponseEntity.ok(response);
    }
}
