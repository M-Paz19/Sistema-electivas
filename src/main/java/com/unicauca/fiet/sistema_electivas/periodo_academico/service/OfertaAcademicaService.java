package com.unicauca.fiet.sistema_electivas.periodo_academico.service;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.electiva.enums.EstadoElectiva;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.EditarCuposDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.OfertaRequestDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.OfertaResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoOferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;

import java.util.List;
/**
 * Servicio de gestión de oferta academica en periodos academicos.
 *
 * Define las operaciones principales sobre la entidad {@link Oferta}.
 */
public interface OfertaAcademicaService {
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
     * <p>El método crea un registro {@link Oferta} vinculado al período y la electiva seleccionada,
     * copia la configuración de programas y cupos de la plantilla, y establece su estado inicial en {@link EstadoOferta#OFERTADA}.</p>
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
     * @return {@link OfertaResponse} con la información detallada de la electiva ofertada.
     * @throws ResourceNotFoundException si no se encuentra el período o la electiva.
     * @throws InvalidStateException si el período no está en estado CONFIGURACION.
     * @throws BusinessException si la electiva no está aprobada o ya existe en la oferta.
     * @throws IllegalArgumentException si los cupos son inválidos o no coinciden con los programas asociados.
     */
    OfertaResponse agregarElectivaOfertada(Long periodoId, OfertaRequestDTO dto);
    /**
     * Edita los cupos por programa de una electiva ofertada.
     *
     * Solo se permite si el período está en estado CONFIGURACION.
     *
     * @param ofertadaId ID de la electiva ofertada
     * @param dto Mapa con los nuevos cupos por programa
     * @return DTO actualizado de la electiva ofertada
     */
    OfertaResponse editarCupos(Long ofertadaId, EditarCuposDTO dto);
    /**
     * Elimina una electiva ofertada de un período.
     *
     * Solo se permite si el período está en estado CONFIGURACION.
     *
     * @param ofertadaId ID de la electiva ofertada
     */
    void eliminarElectivaOfertada(Long ofertadaId);
    /**
     * Lista todas las electivas ofertadas pertenecientes a un período académico.
     *
     * @param periodoId ID del período académico
     * @return Lista de {@link OfertaResponse} con información resumida
     * @throws ResourceNotFoundException si no existe el período académico
     */
    List<OfertaResponse> listarElectivasPorPeriodo(Long periodoId);
}
