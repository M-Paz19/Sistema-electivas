package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.*;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.lang.Nullable;

public interface ValidacionAcademicaService {

    /**
     * Carga y valida los datos provenientes de los archivos SIMCA asociados a un período académico.
     *
     * <p>Este proceso cruza los datos de los estudiantes con las respuestas válidas del formulario,
     * detecta inconsistencias (estudiantes inactivos, no inscritos o con programas no válidos),
     * y actualiza los estados de las respuestas según el resultado de la validación.</p>
     *
     * @param idPeriodo ID del período académico sobre el cual se realiza la carga
     * @param archivos lista de archivos CSV exportados desde SIMCA
     * @return objeto {@link SimcaCargaResponse} con el resumen de la carga y las inconsistencias encontradas
     * @throws ResourceNotFoundException si el período no existe
     * @throws InvalidStateException si el período no está en estado PROCESO_CONFIRMACION_SIMCA
     * @throws BusinessException si ocurre un error al procesar los archivos
     */
    SimcaCargaResponse cargarYValidarDatosSimca(Long idPeriodo, MultipartFile[] archivos);
    /**
     * Obtiene el listado de datos académicos asociados a un período académico específico,
     * con la posibilidad de filtrar los resultados por uno o varios estados de aptitud.
     *
     * <p>Este método permite consultar toda la información académica procesada para los estudiantes
     * en un período dado. Si no se envía ningún filtro de estados, se retornan todos los registros.
     * Si se especifica una lista de estados, únicamente se incluirán aquellos cuya aptitud coincida
     * con los valores solicitados.</p>
     *
     * <p>El resultado incluye datos relevantes del estudiante tales como nombre, programa,
     * créditos aprobados, promedio de carrera, porcentaje de avance y estado del proceso
     * de validación académica.</p>
     *
     * @param periodoId ID del período académico para el cual se desean consultar los datos
     * @param estadosFiltro lista opcional de estados de aptitud para filtrar los resultados;
     *                      si es {@code null} o está vacía, no se aplica filtrado
     * @return lista de objetos {@link DatosAcademicoResponse} con la información académica solicitada
     * @throws ResourceNotFoundException si el período académico no existe
     */
    List<DatosAcademicoResponse> obtenerDatosAcademicosPorPeriodo(Long periodoId,List<EstadoAptitud> estadosFiltro);
    /**
     * Obtiene todas las respuestas del período actual que presentan
     * inconsistencias en la carga de datos de SIMCA, ya sea porque:
     * - El código no se encontró o está inactivo en SIMCA (INCONSISTENTE_SIMCA), o
     * - No se encontró en los archivos cargados de SIMCA (DATOS_NO_CARGADOS).
     */
    List<RespuestaFormularioResponse> obtenerInconsistencias(Long idPeriodo);

    /**
     * Permite resolver una inconsistencia proveniente de SIMCA.
     *
     * <p>Si se decide corregir el código del estudiante, se actualiza el campo
     * {@code codigoEstudiante} y se cambia el estado a {@code DATOS_NO_CARGADOS}.
     * Si se descarta, se cambia el estado a {@code DESCARTADO_SIMCA}.</p>
     *
     * @param respuestaId ID de la respuesta a resolver
     * @param corregir true si se corrige el código, false si se descarta
     * @param nuevoCodigo nuevo código del estudiante (obligatorio si corregir == true)
     * @throws ResourceNotFoundException si no se encuentra la respuesta
     * @throws InvalidStateException si el estado actual no es INCONSISTENTE_SIMCA
     * @throws BusinessException si el nuevo código ya existe en el mismo período académico
     */
    RespuestaFormularioDesicionResponse resolverInconsistenciaSimca(Long respuestaId, boolean corregir, @Nullable String nuevoCodigo);
    /**
     * Regenera un nuevo lote TXT con los códigos corregidos o pendientes de carga,
     * para reenviarlos a SIMCA y completar la información faltante.
     *
     * HU 2.1.2.3 - Generar lote para códigos corregidos.
     */
    String regenerarLoteCorregidos(Long idPeriodo);
    /**
     * Calcula el porcentaje de avance académico para todos los estudiantes de un período.
     *
     * <p>El proceso se ejecuta únicamente cuando el período académico se encuentra
     * en el estado {@code PROCESO_CALCULO_AVANCE}. Antes de iniciar, se valida que no existan
     * registros en estado {@code POSIBLE_NIVELADO}.</p>
     *
     * <p>Para cada registro de datos académicos:
     * <ul>
     *   <li>Si el estudiante está marcado como nivelado, se asigna un avance del 100%.</li>
     *   <li>En caso contrario, el avance se calcula con base en los créditos aprobados
     *       y los créditos totales del plan de estudios, excluyendo las electivas y el trabajo de grado.</li>
     * </ul>
     * Los registros con datos incompletos o inconsistentes son omitidos y contabilizados como errores.</p>
     *
     * <p>Al finalizar el proceso:
     * <ul>
     *   <li>Se actualiza el porcentaje de avance y el estado de cada estudiante a {@code AVANCE_CALCULADO}.</li>
     *   <li>El período cambia de estado a {@code PROCESO_CALCULO_APTITUD}.</li>
     *   <li>Se retorna un resumen con el total de registros procesados, nivelados y con error.</li>
     * </ul></p>
     *
     * @param idPeriodo ID del período académico sobre el cual se realiza el cálculo
     * @return un {@link CambioEstadoValidacionResponse} con el resultado del proceso y el nuevo estado del período
     * @throws ResourceNotFoundException si el período o los registros académicos no existen
     * @throws InvalidStateException si el período no está en estado {@code PROCESO_CALCULO_AVANCE}
     */
    CambioEstadoValidacionResponse calcularPorcentajeAvance(Long idPeriodo);
    /**
     * HU 2.5.1: Valida automáticamente los requisitos académicos generales
     * (porcentaje de avance y semestres cursados) para todos los estudiantes
     * del período académico.
     *
     * <p>El proceso solo puede ejecutarse cuando el período se encuentra en estado
     * {@code PROCESO_CALCULO_APTITUD}. Para cada estudiante se aplican las
     * siguientes reglas:</p>
     *
     * <ul>
     *   <li>Si el estudiante es nivelado ({@code esNivelado = true}),
     *       se marca como {@code APTO} automáticamente.</li>
     *   <li>Si no es nivelado:
     *       <ul>
     *         <li>Debe tener {@code porcentajeAvance >= 65}.</li>
     *         <li>Debe cumplir el requisito de semestres cursados.</li>
     *       </ul>
     *   </li>
     *   <li>Si no cumple las condiciones, se marca como {@code NO_APTO}.</li>
     * </ul>
     *
     * <p>Al finalizar el procesamiento, el período cambia al estado
     * {@code PROCESO_PUBLICACION}.</p>
     *
     * @param periodoId ID del período académico.
     * @return Resumen del procesamiento con cantidades de aptos y no aptos.
     * @throws ResourceNotFoundException si el período no existe.
     * @throws InvalidStateException si el período no está en estado PROCESO_CALCULO_APTITUD.
     */
    CambioEstadoValidacionResponse validarRequisitosGenerales(Long periodoId);
}