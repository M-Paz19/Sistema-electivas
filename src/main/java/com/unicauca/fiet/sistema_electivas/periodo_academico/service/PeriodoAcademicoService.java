package com.unicauca.fiet.sistema_electivas.periodo_academico.service;



import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.AgregarElectivaOfertadaDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.CrearPeriodoAcademicoDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.ElectivaOfertadaResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.PeriodoAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;

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

    ElectivaOfertadaResponse agregarElectivaOfertada(Long periodoId, AgregarElectivaOfertadaDTO dto);
    /**
     * Lista todos los periodos académicos existentes en el sistema, opcionalmente filtrados por estado.
     *
     * @param estado (opcional) Estado de los periodos a listar (por ejemplo: CONFIGURACION, ACTIVO, CERRADO).
     * @return Lista de {@link PeriodoAcademico}.
     */
    //List<PeriodoAcademico> listarPeriodos(EstadoPeriodoAcademico estado);

    /**
     * Busca un periodo académico por su identificador único.
     *
     * @param id Identificador del periodo académico.
     * @return {@link PeriodoAcademico} si existe.
     * @throws ResourceNotFoundException si no se encuentra el periodo.
     */
    //PeriodoAcademico obtenerPorId(Long id);

    /**
     * Cambia el estado de un periodo académico (por ejemplo, de CONFIGURACION a ACTIVO o CERRADO).
     *
     * <p>Validaciones:
     * <ul>
     *   <li>El periodo debe existir.</li>
     *   <li>El cambio de estado debe ser válido (por ejemplo, no se puede reactivar un periodo cerrado).</li>
     * </ul>
     *
     * @param id ID del periodo académico.
     * @param nuevoEstado Estado destino.
     * @return {@link PeriodoAcademico} actualizado.
     * @throws ResourceNotFoundException si el periodo no existe.
     * @throws InvalidStateException si la transición de estado no está permitida.
     */
    //PeriodoAcademico cambiarEstado(Long id, EstadoPeriodoAcademico nuevoEstado);

    /**
     * Elimina (lógicamente o físicamente) un periodo académico, según la política de negocio.
     *
     * <p>Validaciones:
     * <ul>
     *   <li>El periodo debe existir.</li>
     *   <li>No debe estar en uso por otros registros dependientes (por ejemplo, inscripciones o planes activos).</li>
     * </ul>
     *
     * @param id ID del periodo académico a eliminar.
     * @throws ResourceNotFoundException si no existe el periodo.
     * @throws InvalidStateException si el periodo no puede eliminarse.
     */
    //void eliminarPeriodo(Long id);
}
