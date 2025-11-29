package com.unicauca.fiet.sistema_electivas.periodo_academico.service;



import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.*;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import io.opencensus.resource.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Servicio de gestión de periodos académicos.
 *
 * Define las operaciones principales sobre la entidad {@link PeriodoAcademico}.
 */
public interface PeriodoAcademicoService {

    /**
     * Crea un nuevo periodo académico en el sistema.
     *
     * <p>Validaciones:
     * <ul>
     *   <li>El identificador del semestre debe tener el formato <b>20XX-1</b> o <b>20XX-2</b>.</li>
     *   <li>No debe existir otro periodo con el mismo semestre.</li>
     *   <li>La fecha de apertura no puede ser posterior a la de cierre.</li>
     *   <li>Las fechas deben pertenecer al rango permitido según el semestre:
     *       <ul>
     *           <li>Para <b>20XX-1</b>: apertura entre julio del año anterior y cierre hasta junio del año actual.</li>
     *           <li>Para <b>20XX-2</b>: apertura y cierre dentro del mismo año (enero a diciembre).</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p>El nuevo periodo se crea con estado inicial {@link EstadoPeriodoAcademico#CONFIGURACION}.
     *
     * @param dto Datos del nuevo periodo académico.
     * @return {@link PeriodoAcademico} creado y persistido.
     * @throws DuplicateResourceException si ya existe un periodo con el mismo semestre.
     * @throws IllegalArgumentException si las fechas o el formato del semestre son inválidos.
     */
    PeriodoAcademicoResponse crearPeriodo(CrearPeriodoAcademicoDTO dto);

    /**
     * Abre un período académico, cambiando su estado de {@code CONFIGURACION} a {@code ABIERTO},
     * siempre que cumpla las condiciones establecidas.
     *
     * <p>Validaciones realizadas:</p>
     * <ul>
     *   <li>Debe existir al menos una {@code Oferta} asociada en estado {@code OFERTADA}.</li>
     *   <li>No debe existir otro período en estado {@code ABIERTO} o {@code EN_ASIGNACION}.</li>
     *   <li>Si la fecha actual es anterior a la {@code fechaApertura}, solo se permite abrirlo si {@code forzarApertura} es {@code true}.</li>
     * </ul>
     *
     * <p>Al abrir el período, se genera (o habilita) el formulario de preinscripción y
     * se bloquean las opciones de edición de configuración.</p>
     *
     * @param periodoId ID del período académico a abrir
     * @param forzarApertura indica si se debe permitir la apertura antes de la fecha programada
     * @return {@link CambioEstadoResponse} con el nuevo estado y mensaje de confirmación
     * @throws ResourceNotFoundException si no existe el período con el ID indicado
     * @throws BusinessException si no se cumplen las condiciones para la apertura
     */
    CambioEstadoResponse abrirPeriodo(Long periodoId, boolean forzarApertura, Map<Long, Integer> opcionesPorPrograma);

    /**
     * Obtiene una lista de períodos académicos con información resumida para la tabla.
     *
     * @param semestreTexto Texto parcial para filtrar por semestre (opcional)
     * @param estado Estado a filtrar (opcional)
     * @return Lista de DTOs {@link PeriodoAcademicoResponse} con detalles y cantidad de electivas
     */
    List<PeriodoAcademicoResponse> listarPeriodos(String semestreTexto, EstadoPeriodoAcademico estado);

    /**
     * Cierra el formulario de preinscripción asociado a un período académico.
     *
     * <p>Acciones realizadas:
     * <ul>
     *   <li>Verifica que el período esté en estado {@code ABIERTO} antes de cerrarlo.</li>
     *   <li>Cambia el estado a {@code EN_PROCESO_ASIGNACION} y guarda el cambio.</li>
     *   <li>Extrae el {@code formId} desde la URL del formulario de Google asociada al período.</li>
     *   <li>Obtiene automáticamente las respuestas del formulario mediante la API de Google Forms.</li>
     *   <li>Si no existe una URL configurada, cambia el estado a {@code CONFIGURACION} y lanza excepción.</li>
     * </ul>
     *
     * @param periodoId ID del período académico a cerrar.
     * @return El objeto {@link PeriodoAcademico} actualizado con el nuevo estado.
     * @throws InvalidStateException Si el período no está en estado ABIERTO o no tiene un formulario asociado.
     * @throws RuntimeException Si ocurre un error durante el cierre o la obtención de respuestas.
     */
    CambioEstadoResponse cerrarFormulario(Long periodoId);
    /**
     * Carga manualmente las respuestas del formulario de preinscripción de un período académico.
     *
     * <p>Esta función actúa como mecanismo alternativo cuando la obtención automática desde Google Forms falla
     * o se requiere un override manual. Permite subir un archivo con las respuestas (formato Excel o CSV) y
     * procesarlas directamente en el sistema.</p>
     *
     * <p>Acciones realizadas:
     * <ul>
     *   <li>Valida que el período esté en estado {@code ABIERTO_FORMULARIO} antes de cargar respuestas.</li>
     *   <li>Parsea el archivo recibido, verificando su formato y estructura esperada.</li>
     *   <li>Guarda una copia física del archivo como respaldo histórico.</li>
     *   <li>Procesa las respuestas y las almacena en la base de datos.</li>
     *   <li>Cambia el estado del período a {@code CERRADO_FORMULARIO}.</li>
     * </ul>
     *
     * @param periodoId ID del período académico sobre el que se realiza la carga manual.
     * @param file Archivo subido por el usuario, conteniendo las respuestas del formulario.
     * @return Objeto {@link CambioEstadoResponse} con la información del nuevo estado del período.
     * @throws ResourceNotFoundException Si el período indicado no existe.
     * @throws InvalidStateException Si el período no está en estado {@code ABIERTO_FORMULARIO}.
     * @throws BusinessException Si el archivo es inválido o presenta errores de formato.
     */
    CambioEstadoResponse cargarRespuestasManual(Long periodoId, MultipartFile file);

    /**
     * Cierra definitivamente el proceso de asignación de un período académico.
     *
     * <p>Acciones realizadas:
     * <ul>
     *   <li>Valida que el período esté en estado {@code EN_PROCESO_ASIGNACION}.</li>
     *   <li>Cambia el estado del período a {@code CERRADO}.</li>
     *   <li>Actualiza todas las {@code Oferta} asociadas a estado {@code CERRADA}.</li>
     *   <li>Establece la fecha de actualización correspondiente.</li>
     *   <li>Garantiza que, una vez cerrado, el período solo permite consultas y exportaciones.</li>
     * </ul>
     *
     * @param periodoId identificador del período académico.
     * @return {@link CambioEstadoResponse} con el nuevo estado del período.
     * @throws ResourceNotFoundException si el período no existe.
     * @throws InvalidStateException si el período no está en estado EN_PROCESO_ASIGNACION.
     */
    CambioEstadoResponse cerrarPeriodoAcademico(Long periodoId);

}
