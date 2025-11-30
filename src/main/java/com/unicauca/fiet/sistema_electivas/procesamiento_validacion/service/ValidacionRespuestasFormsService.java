package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CambioEstadoValidacionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioDesicionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioResponse;
import org.springframework.lang.Nullable;

import java.util.List;

public interface ValidacionRespuestasFormsService {
    /**
     * Obtiene todas las respuestas asociadas a un período académico específico.
     *
     * @param periodoId ID del período académico
     * @return Lista de respuestas del formulario vinculadas a ese período.
     */
    List<RespuestaFormularioResponse> obtenerRespuestasPorPeriodo(Long periodoId);

    /**
     * Aplica el filtro de duplicados sobre las respuestas de un período académico.
     *
     * <p>Acciones realizadas:
     * <ul>
     *   <li>Verifica que el período esté en estado {@code CERRADO_FORMULARIO} antes de iniciar el filtrado.</li>
     *   <li>Obtiene todas las respuestas en estado {@code SIN_PROCESAR} asociadas al período.</li>
     *   <li>Agrupa las respuestas por {@code codigoEstudiante} y conserva únicamente la más antigua según {@code timestampRespuesta}.</li>
     *   <li>Marca las respuestas duplicadas con el estado {@code DUPLICADO} y las válidas con el estado {@code UNICO}.</li>
     *   <li>Actualiza los registros en la base de datos y cambia el estado del período a {@code PROCESO_FILTRADO_DUPLICADOS}.</li>
     * </ul>
     *
     * @param idPeriodo ID del período académico sobre el cual se aplicará el filtro.
     * @throws ResourceNotFoundException Si no se encuentra el período académico con el ID indicado.
     * @throws InvalidStateException Si el período no está en estado {@code CERRADO_FORMULARIO} o no hay respuestas sin procesar.
     */
    CambioEstadoValidacionResponse aplicarFiltroDuplicados(Long idPeriodo);
    /**
     * Aplica el filtro de antigüedad sobre las respuestas únicas de un período académico.
     *
     * <p>Acciones realizadas:
     * <ul>
     *   <li>Verifica que el período esté en estado {@code PROCESO_FILTRADO_DUPLICADOS} antes de aplicar el filtro.</li>
     *   <li>Obtiene todas las respuestas con estado {@code UNICO} asociadas al período.</li>
     *   <li>Valida el formato del código estudiantil (ej: {@code 104621011351}).</li>
     *   <li>Calcula los semestres cursados a partir del año y periodo de ingreso codificados en el número del estudiante.</li>
     *   <li>Clasifica las respuestas como:
     *       <ul>
     *         <li>{@code CUMPLE} si tiene 6 o más semestres cursados,</li>
     *         <li>{@code NO_CUMPLE} si tiene menos de 6,</li>
     *         <li>{@code FORMATO_INVALIDO} si el código no cumple el patrón esperado.</li>
     *       </ul>
     *   </li>
     *   <li>Persiste los cambios en las respuestas y actualiza el estado del período a {@code PROCESO_CLASIFICACION_ANTIGUEDAD}.</li>
     * </ul>
     *
     * @param idPeriodo ID del período académico sobre el cual se aplicará el filtro.
     * @return {@link CambioEstadoValidacionResponse} con el nuevo estado del período y un mensaje descriptivo.
     * @throws ResourceNotFoundException Si no se encuentra el período académico con el ID indicado.
     * @throws InvalidStateException Si el período no está en estado {@code PROCESO_FILTRADO_DUPLICADOS}
     *                               o no existen respuestas únicas válidas.
     */
    CambioEstadoValidacionResponse aplicarFiltroCodigosPorAntiguedad(Long idPeriodo);

    /**
     * Permite la decisión manual sobre respuestas con formato de código desconocido.
     *
     * <p>Solo se pueden modificar respuestas que se encuentren en estado
     * {@code FORMATO_INVALIDO}, resultado del filtro de antigüedad.</p>
     *
     * <p>Dependiendo del parámetro {@code incluir}, el registro se marca como:
     * <ul>
     *   <li>{@code INCLUIDO_MANUAL} si el valor es true.</li>
     *   <li>{@code DESCARTADO_MANUAL} si el valor es false.</li>
     * </ul>
     * </p>
     *
     * @param respuestaId ID de la respuesta que se desea modificar.
     * @param incluir true para incluir, false para descartar.
     * @return {@link RespuestaFormularioResponse} con la información actualizada.
     * @throws ResourceNotFoundException Si no se encuentra la respuesta con el ID indicado.
     * @throws InvalidStateException Si la respuesta no se encuentra en estado {@code FORMATO_INVALIDO}.
     */
    RespuestaFormularioDesicionResponse revisarManualFormatoInvalido(Long respuestaId, boolean incluir,  @Nullable String nuevoCodigo);

    /**
     * Confirma la lista final de estudiantes para la fase de validación con SIMCA.
     *
     * <p>Antes de confirmar, el sistema verifica que no existan respuestas con estado
     * {@code FORMATO_INVALIDO}, lo que significaría que aún hay registros pendientes de revisión manual.</p>
     *
     * <p>Si todos los registros fueron revisados (solo quedan estados
     * {@code CUMPLE}, {@code INCLUIDO_MANUAL} o {@code DESCARTADO_MANUAL}),
     * el estado del período cambia a {@code PROCESO_CONFIRMACION_SIMCA}.</p>
     *
     * @param idPeriodo ID del período académico.
     * @return {@link CambioEstadoValidacionResponse} con el nuevo estado del período y un mensaje resumen.
     * @throws ResourceNotFoundException Si el período no existe.
     * @throws InvalidStateException Si todavía existen registros pendientes de revisión manual.
     */
    CambioEstadoValidacionResponse confirmarListaParaSimca(Long idPeriodo);


}
