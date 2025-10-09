package com.unicauca.fiet.sistema_electivas.programa.service;

import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ProgramaElectivaRepository;
import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaDisableResponse;
import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaRequest;
import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaResponse;
import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaUpdateRequest;
import com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.programa.mapper.ProgramaMapper;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import com.unicauca.fiet.sistema_electivas.programa.repository.ProgramaRepository;
import com.unicauca.fiet.sistema_electivas.electiva.enums.EstadoElectiva;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramaServiceImpl implements ProgramaService {
    private final ProgramaRepository programaRepository;
    private final ProgramaElectivaRepository programaElectivaRepository;
    /**
     * Crea un nuevo programa académico en el sistema.
     *
     * <p>Validaciones realizadas:
     * <ul>
     *   <li>El código del programa debe ser único.</li>
     *   <li>El nombre del programa debe ser único.</li>
     * </ul>
     *
     * <p>El estado inicial de todo programa creado será {@link EstadoPrograma#PENDIENTE_PLAN}.
     *
     * @param request Objeto con los datos del programa (código, nombre).
     * @return {@link ProgramaResponse} con la información del programa creado.
     * @throws DuplicateResourceException Si ya existe un programa con el mismo código o nombre.
     */
    @Override
    public ProgramaResponse crearPrograma(ProgramaRequest request) {
        // 1️ Validar duplicados por código
        programaRepository.findByCodigo(request.getCodigo())
                .ifPresent(p -> {
                    throw new DuplicateResourceException(
                            "El código " + request.getCodigo() + " ya está en uso. Por favor, utilice otro."
                    );
                });

        // 2️ Validar duplicados por nombre
        programaRepository.findByNombre(request.getNombre())
                .ifPresent(p -> {
                    throw new DuplicateResourceException(
                            "El nombre " + request.getNombre() + " ya está en uso. Por favor, utilice otro."
                    );
                });

        // 3️ Crear la entidad a partir del DTO usando el mapper
        Programa programa = ProgramaMapper.toEntity(request);

        // Forzar estado de creacion aquí:
        programa.setEstado(EstadoPrograma.PENDIENTE_PLAN);

        // 4️ Guardar la entidad
        Programa saved = programaRepository.save(programa);

        // 5️⃣ Convertir la entidad guardada a DTO de respuesta
        return ProgramaMapper.toResponse(saved);
    }

    /**
     * Edita los datos de un programa existente (solo el nombre).
     *
     * <p>Validaciones realizadas:
     * <ul>
     *   <li>Debe existir un programa con el ID dado.</li>
     *   <li>No se permite editar programas en estado {@link EstadoPrograma#DESHABILITADO}.</li>
     *   <li>El nombre es obligatorio y no puede estar vacío.</li>
     *   <li>El nombre debe ser único (salvo para el mismo programa).</li>
     * </ul>
     *
     * @param id Identificador del programa a editar.
     * @param request Datos con el nuevo nombre.
     * @return {@link ProgramaResponse} con la información actualizada.
     * @throws ResourceNotFoundException Si el programa no existe.
     * @throws IllegalStateException Si el programa está deshabilitado.
     * @throws IllegalArgumentException Si el campo obligatorio está vacío.
     * @throws DuplicateResourceException Si el nombre ya está en uso por otro programa.
     */
    @Override
    public ProgramaResponse editarPrograma(Long id, ProgramaUpdateRequest request) {
        // 1️ Buscar el programa existente
        Programa programa = programaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Programa con id " + id + " no encontrado"
                ));

        // 2️ Validar que no esté deshabilitado
        if (programa.getEstado() == EstadoPrograma.DESHABILITADO) {
            throw new InvalidStateException("No se puede editar un programa deshabilitado.");
        }

        // 3️ Validar campo obligatorio
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new BusinessException("Complete todos los campos obligatorios.");
        }

        // 4️ Validar duplicado en nombre
        programaRepository.findByNombre(request.getNombre())
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    throw new DuplicateResourceException(
                            "El nombre " + request.getNombre() + " ya está en uso. Por favor, utilice otro."
                    );
                });

        // 5️ Actualizar la entidad usando el mapper
        ProgramaMapper.updateEntity(programa, request);

        // 6️ Guardar cambios
        Programa actualizado = programaRepository.save(programa);

        // 7️ Convertir a DTO de respuesta usando el mapper
        return ProgramaMapper.toResponse(actualizado);
    }

    /**
     * Deshabilita un programa académico, cambiando su estado a {@link EstadoPrograma#DESHABILITADO}.
     *
     * <p>Validaciones realizadas:
     * <ul>
     *   <li>Debe existir un programa con el ID dado.</li>
     *   <li>No debe tener electivas activas asociadas (estado distinto a DESHABILITADA).</li>
     * </ul>
     *
     * @param id Identificador del programa a deshabilitar.
     * @return {@link ProgramaDisableResponse} con el resultado de la operación.
     * @throws ResourceNotFoundException Si el programa no existe.
     * @throws BusinessException Si existen electivas activas asociadas.
     */
    @Override
    public ProgramaDisableResponse deshabilitarPrograma(Long id) {
        // 1️ Buscar el programa
        Programa programa = programaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programa con id " + id + " no encontrado"));

        // 2️ Validar si ya está deshabilitado
        if (programa.getEstado() == EstadoPrograma.DESHABILITADO) {
            throw new InvalidStateException("El programa ya está deshabilitado.");
        }

        // 3️ Validar electivas activas asociadas
        int electivasActivas = programaElectivaRepository.countElectivasActivasByProgramaId(id, EstadoElectiva.APROBADA);
        if (electivasActivas > 0) {
            throw new BusinessException("No se puede deshabilitar el programa porque tiene "
                    + electivasActivas + " electivas activas asociadas. Desasócialas o deshabilítalas primero.");
        }

        // 4️ Deshabilitar el programa
        programa.setEstado(EstadoPrograma.DESHABILITADO);
        programaRepository.save(programa);

        // 5️ Retornar respuesta
        return new ProgramaDisableResponse(
                programa.getId(),
                programa.getNombre()
        );
    }
    /**
     * Lista todos los programas académicos registrados en la base de datos.
     *
     * @return Lista de {@link ProgramaResponse} con la información de cada programa.
     */
    @Override
    public List<ProgramaResponse> listarProgramas() {
        return programaRepository.findAll().stream()
                .map(ProgramaMapper::toResponse)
                .toList();
    }

    /**
     * Busca programas académicos por su estado actual.
     *
     * @param estado Estado del programa (ejemplo: {@link EstadoPrograma#APROBADO}).
     * @return Lista de {@link ProgramaResponse} que cumplen con el estado solicitado.
     */
    @Override
    public List<ProgramaResponse> buscarPorEstado(EstadoPrograma estado) {
        return programaRepository.findByEstado(estado).stream()
                .map(ProgramaMapper::toResponse)
                .toList();
    }

    /**
     * Busca programas académicos cuyo nombre contenga parcial o totalmente
     * el texto ingresado (ignora mayúsculas y minúsculas).
     *
     * @param nombre Texto a buscar en el nombre de los programas.
     * @return Lista de {@link ProgramaResponse} encontrados.
     */
    @Override
    public List<ProgramaResponse> buscarPorNombre(String nombre) {
        return programaRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(ProgramaMapper::toResponse)
                .toList();
    }

    /**
     * Busca programas académicos cuyo código contenga parcial o totalmente
     * el texto ingresado (ignora mayúsculas y minúsculas).
     *
     * @param codigo Texto a buscar en el código de los programas.
     * @return Lista de {@link ProgramaResponse} encontrados.
     */
    @Override
    public List<ProgramaResponse> buscarPorCodigo(String codigo) {
        return programaRepository.findByCodigoContainingIgnoreCase(codigo).stream()
                .map(ProgramaMapper::toResponse)
                .toList();
    }



}
