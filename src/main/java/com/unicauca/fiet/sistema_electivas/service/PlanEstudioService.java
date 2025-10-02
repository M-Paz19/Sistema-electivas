package com.unicauca.fiet.sistema_electivas.service;

import com.unicauca.fiet.sistema_electivas.dto.*;
import com.unicauca.fiet.sistema_electivas.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.model.PlanEstudio;
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
    List<PlanEstudioListResponse> listarPlanesPorPrograma(Long programaId);
    /**
     * Procesa la subida de la malla curricular (archivo Excel) para un plan.
     *
     * @param programaId ID del programa (valida que el plan pertenezca a este programa)
     * @param planId     ID del plan de estudios
     * @param file       Archivo Excel (.xlsx) con columnas: codigo,nombre,creditos,semestre
     * @param configuracion informacion de la configuración del plan
     * @return MallaUploadResponse con resumen del procesamiento
     */
    public MallaUploadResponse cargarMallaCurricular(
            Long programaId,
            Long planId,
            MultipartFile file,
            ConfiguracionPlanRequest configuracion
    );
}
