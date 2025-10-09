package com.unicauca.fiet.sistema_electivas.periodo_academico.service;



import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.electiva.enums.EstadoElectiva;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.AgregarElectivaOfertadaDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.CrearPeriodoAcademicoDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.ElectivaOfertadaResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.PeriodoAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.ElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;

import java.util.List;

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
     * Agrega una nueva electiva a la oferta académica de un período específico.
     *
     * <p>Validaciones:
     * <ul>
     *   <li>El período debe existir y estar en estado {@link EstadoPeriodoAcademico#CONFIGURACION}.</li>
     *   <li>La electiva debe existir y estar en estado aprobado ({@link EstadoElectiva#APROBADA}).</li>
     *   <li>No debe existir previamente una oferta de la misma electiva en este período.</li>
     *   <li>Se validan los cupos por programa, asegurando que coincidan con los programas asociados a la electiva y que la suma total sea 18.</li>
     * </ul>
     *
     * <p>El método crea un registro {@link ElectivaOfertada} vinculado al período y la electiva seleccionada,
     * copia la configuración de programas y cupos de la plantilla, y establece su estado inicial en {@link EstadoElectivaOfertada#OFERTADA}.</p>
     *
     * <p>HU1.4.2 - Configurar la oferta académica de un período:</p>
     * <ul>
     *   <li>Escenario 1: Agregar electiva exitosamente → Se crea la oferta y se retorna la información detallada.</li>
     *   <li>Escenario 2: Electiva ya existe en la oferta → Lanza excepción con mensaje de advertencia.</li>
     *   <li>Escenario 3: Solo electivas aprobadas aparecen en el buscador → Controlado por filtro de estado en la búsqueda.</li>
     *   <li>Escenario 4: Período no editable → Lanza excepción si el período no está en estado CONFIGURACION.</li>
     * </ul>
     *
     * @param periodoId ID del período académico donde se agregará la electiva.
     * @param dto DTO con la información de la electiva a ofertar y los cupos por programa.
     * @return {@link ElectivaOfertadaResponse} con la información detallada de la electiva ofertada.
     * @throws ResourceNotFoundException si no se encuentra el período o la electiva.
     * @throws InvalidStateException si el período no está en estado CONFIGURACION.
     * @throws BusinessException si la electiva no está aprobada o ya existe en la oferta.
     * @throws IllegalArgumentException si los cupos son inválidos o no coinciden con los programas asociados.
     */
    ElectivaOfertadaResponse agregarElectivaOfertada(Long periodoId, AgregarElectivaOfertadaDTO dto);
    /**
     * Obtiene una lista de períodos académicos con información resumida para la tabla.
     *
     * @param semestreTexto Texto parcial para filtrar por semestre (opcional)
     * @param estado Estado a filtrar (opcional)
     * @return Lista de DTOs {@link PeriodoAcademicoResponse} con detalles y cantidad de electivas
     */
    List<PeriodoAcademicoResponse> listarPeriodos(String semestreTexto, EstadoPeriodoAcademico estado);
    /**
     * Lista todas las electivas ofertadas pertenecientes a un período académico.
     *
     * @param periodoId ID del período académico
     * @return Lista de {@link ElectivaOfertadaResponse} con información resumida
     * @throws ResourceNotFoundException si no existe el período académico
     */
    public List<ElectivaOfertadaResponse> listarElectivasPorPeriodo(Long periodoId);
}
