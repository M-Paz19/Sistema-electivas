package com.unicauca.fiet.sistema_electivas.plan_estudio.service;

import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.*;
import com.unicauca.fiet.sistema_electivas.plan_estudio.enums.EstadoPlanEstudio;
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import jakarta.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Servicio de gestión de planes de estudio.
 *
 * Define las operaciones principales sobre la entidad {@link PlanEstudio}.
 */
public interface PlanEstudioService {

    /**
     * Crea un nuevo plan de estudio asociado a un programa.
     *
     * <p>Validaciones:
     * <ul>
     *   <li>El nombre del plan debe ser único dentro del mismo programa.</li>
     *   <li>Todos los campos obligatorios deben estar completos.</li>
     * </ul>
     *
     * @param programaId Identificador del programa al cual se asociará el plan.
     * @param request Datos del nuevo plan.
     * @return {@link PlanEstudioResponse} con la información del plan creado.
     * @throws DuplicateResourceException si ya existe un plan con el mismo nombre en el programa.
     * @throws ResourceNotFoundException si el programa no existe.
     */
    PlanEstudioResponse crearPlan(Long programaId, PlanEstudioRequest request);
    /**
     * Lista todos los planes de estudio de un programa.
     *
     * @param programaId Identificador del programa.
     * @return Lista de {@link PlanEstudioResponse}.
     * @throws ResourceNotFoundException si el programa no existe.
     */
    List<PlanEstudioListResponse> listarPlanesPorPrograma(Long programaId, @Nullable EstadoPlanEstudio estado);

    /**
     * Lista todos los planes de estudio, con opción de filtrar por estado.
     *
     * @param estado (opcional) Estado del plan.
     * @return Lista de {@link PlanEstudioListResponse}.
     */
    List<PlanEstudioListResponse> listarTodosLosPlanes(@Nullable EstadoPlanEstudio estado);

    /**
     * Actualiza la información de un plan de estudio existente según su estado.
     *
     * <p>Reglas de edición:
     * <ul>
     *   <li>Si el plan se encuentra en estado {@link EstadoPlanEstudio#CONFIGURACION_PENDIENTE},
     *       se permiten modificaciones en los campos:
     *       <b>nombre</b>, <b>versión</b>, <b>vigenciaInicio</b> y <b>vigenciaFin</b>.</li>
     *   <li>Si el plan se encuentra en estado {@link EstadoPlanEstudio#ACTIVO},
     *       solo puede modificarse el campo <b>vigenciaFin</b>.</li>
     *   <li>Si el plan está en cualquier otro estado, no se permite su edición.</li>
     * </ul>
     *
     * <p>Validaciones:
     * <ul>
     *   <li>El plan debe existir en el sistema.</li>
     *   <li>El rango de vigencia (inicio y fin) debe ser válido
     *       (la fecha de fin no puede ser anterior a la fecha de inicio).</li>
     *   <li>El nombre del plan debe ser único dentro del programa,
     *       si se modifica mientras está en configuración pendiente.</li>
     * </ul>
     *
     * @param planId Identificador del plan de estudio a modificar.
     * @param request Datos actualizados del plan de estudio.
     * @return {@link PlanEstudioResponse} con la información actualizada del plan.
     * @throws ResourceNotFoundException si no existe un plan con el ID proporcionado.
     * @throws BusinessException si el estado del plan no permite edición o si las validaciones de negocio fallan.
     */
    PlanEstudioResponse actualizarPlan(Long programaId, Long planId, PlanEstudioRequest request);

    /**
     * Desactiva un plan de estudio, cambiando su estado a {@link EstadoPlanEstudio#INACTIVO}.
     *
     * <p>Validaciones:
     * <ul>
     *   <li>El plan debe existir y estar en estado ACTIVO.</li>
     *   <li>No puede ser el único plan activo del programa.</li>
     *   <li>Si el plan no tiene fecha de vigencia fin, se asigna la fecha actual.</li>
     * </ul>
     *
     * @param planId Identificador del plan de estudio a desactivar.
     * @return {@link PlanEstudioResponse} con los datos actualizados del plan.
     * @throws ResourceNotFoundException si el plan no existe.
     * @throws InvalidStateException si el plan no puede ser desactivado.
     */
    public PlanEstudioResponse desactivarPlan(Long planId);

    /**
     * Procesa la subida de la malla curricular (archivo Excel) para un plan.
     *
     * @param programaId ID del programa (valida que el plan pertenezca a este programa)
     * @param planId     ID del plan de estudios
     * @param file       Archivo Excel (.xlsx) con columnas: codigo,nombre,creditos,semestre
     * @param configuracion informacion de la configuración del plan
     * @return MallaUploadResponse con resumen del procesamiento
     */
    MallaUploadResponse cargarMallaCurricular(
            Long programaId,
            Long planId,
            MultipartFile file,
            ConfiguracionPlanRequest configuracion
    );
}
