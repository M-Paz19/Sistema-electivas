package com.unicauca.fiet.sistema_electivas.service;

import com.unicauca.fiet.sistema_electivas.dto.ProgramaDisableResponse;
import com.unicauca.fiet.sistema_electivas.dto.ProgramaRequest;
import com.unicauca.fiet.sistema_electivas.dto.ProgramaResponse;
import com.unicauca.fiet.sistema_electivas.dto.ProgramaUpdateRequest;
import com.unicauca.fiet.sistema_electivas.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.model.Programa;

import java.util.List;

/**
 * Servicio de gestión de programas académicos.
 *
 * <p>Define las operaciones principales sobre la entidad {@link Programa}:
 * <ul>
 *   <li>Crear un programa académico.</li>
 *   <li>Editar un programa existente.</li>
 *   <li>Deshabilitar un programa (cambio de estado).</li>
 * </ul>
 */
public interface ProgramaService {

    /**
     * Crea un nuevo programa académico.
     *
     * @param request Objeto con los datos del programa (código y nombre).
     * @return {@link ProgramaResponse} con la información del programa creado.
     * @throws DuplicateResourceException si el código o nombre ya existen.
     */
    ProgramaResponse crearPrograma(ProgramaRequest request);

    /**
     * Edita un programa académico existente (actualmente solo permite cambiar el nombre).
     *
     * @param id Identificador del programa a editar.
     * @param request Datos con el nuevo nombre.
     * @return {@link ProgramaResponse} con la información del programa actualizado.
     * @throws ResourceNotFoundException si no existe el programa.
     * @throws IllegalStateException si el programa está deshabilitado.
     * @throws IllegalArgumentException si el campo obligatorio está vacío.
     * @throws DuplicateResourceException si el nombre ya está en uso por otro programa.
     */
    ProgramaResponse editarPrograma(Long id, ProgramaUpdateRequest request);

    /**
     * Deshabilita un programa académico, cambiando su estado a
     * {@link EstadoPrograma#DESHABILITADO}.
     *
     * @param id Identificador del programa a deshabilitar.
     * @return {@link ProgramaDisableResponse} con el resultado de la operación.
     * @throws ResourceNotFoundException si no existe el programa.
     * @throws BusinessException si tiene electivas activas asociadas (validación futura).
     */
    ProgramaDisableResponse deshabilitarPrograma(Long id);

    /**
     * Lista todos los programas académicos registrados.
     *
     * @return Lista de {@link ProgramaResponse}.
     */
    List<ProgramaResponse> listarProgramas();

    /**
     * Busca programas académicos filtrados por su estado.
     *
     * @param estado Estado del programa (ejemplo: APROBADO, DESHABILITADO).
     * @return Lista de {@link ProgramaResponse} que cumplen el criterio.
     */
    List<ProgramaResponse> buscarPorEstado(EstadoPrograma estado);

    /**
     * Busca programas académicos cuyo nombre coincida parcial o totalmente
     * con el texto ingresado.
     *
     * @param nombre Texto a buscar dentro del nombre de los programas.
     * @return Lista de {@link ProgramaResponse} encontrados.
     */
    List<ProgramaResponse> buscarPorNombre(String nombre);

    /**
     * Busca programas académicos cuyo código coincida parcial o totalmente
     * con el texto ingresado.
     *
     * @param codigo Texto a buscar dentro del código de los programas.
     * @return Lista de {@link ProgramaResponse} encontrados.
     */
    List<ProgramaResponse> buscarPorCodigo(String codigo);
}
