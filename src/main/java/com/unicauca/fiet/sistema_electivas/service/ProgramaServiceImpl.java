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
import com.unicauca.fiet.sistema_electivas.repository.ProgramaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramaServiceImpl implements ProgramaService {
    private final ProgramaRepository programaRepository;
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
        // Validar duplicados por código
        programaRepository.findByCodigo(request.getCodigo())
                .ifPresent(p -> { throw new DuplicateResourceException("El código " + request.getCodigo() + " ya está en uso. Por favor, utilice otro."); });

        // Validar duplicados por nombre
        programaRepository.findByNombre(request.getNombre())
                .ifPresent(p -> { throw new DuplicateResourceException("El nombre " + request.getNombre() + " ya está en uso. Por favor, utilice otro."); });

        // Crear y configurar el programa
        Programa programa = new Programa();
        programa.setCodigo(request.getCodigo());
        programa.setNombre(request.getNombre());
        programa.setEstado(EstadoPrograma.PENDIENTE_PLAN); // Estado inicial

        Programa saved = programaRepository.save(programa);

        return new ProgramaResponse(
                saved.getId(),
                saved.getCodigo(),
                saved.getNombre(),
                saved.getEstado().getDescripcion(),
                "Programa creado exitosamente"
        );
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
        // 1. Buscar el programa
        Programa programa = programaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programa con id " + id + " no encontrado"));

        // 2. Validar que no esté deshabilitado
        if (programa.getEstado() == EstadoPrograma.DESHABILITADO) {
            throw new IllegalStateException("No se puede editar un programa deshabilitado.");
        }

        // 3. Validar campo obligatorio
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new IllegalArgumentException("Complete todos los campos obligatorios.");
        }

        // 4. Validar duplicado en nombre
        programaRepository.findByNombre(request.getNombre())
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    throw new DuplicateResourceException(
                            "El nombre " + request.getNombre() + " ya está en uso. Por favor, utilice otro."
                    );
                });

        // 5. Actualizar
        programa.setNombre(request.getNombre());
        Programa actualizado = programaRepository.save(programa);

        // 6. Respuesta
        return new ProgramaResponse(
                actualizado.getId(),
                actualizado.getCodigo(),
                actualizado.getNombre(),
                actualizado.getEstado().getDescripcion(),
                "Programa actualizado exitosamente"
        );
    }
    /**
     * Deshabilita un programa académico, cambiando su estado a {@link EstadoPrograma#DESHABILITADO}.
     *
     * <p>Validaciones realizadas:
     * <ul>
     *   <li>Debe existir un programa con el ID dado.</li>
     *   <li>(Pendiente) No debe tener electivas activas asociadas.</li>
     * </ul>
     *
     * @param id Identificador del programa a deshabilitar.
     * @return {@link ProgramaDisableResponse} con el resultado de la operación.
     * @throws ResourceNotFoundException Si el programa no existe.
     * @throws BusinessException (comentado por ahora) Si existen electivas activas asociadas.
     */
    @Override
    public ProgramaDisableResponse deshabilitarPrograma(Long id) {
        Programa programa = programaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programa con id " + id + " no encontrado"));

        // 🔒 Validación futura: electivas activas
        /**int electivasActivas = electivaRepository.countByProgramaIdAndEstadoNot(id, EstadoElectiva.DESHABILITADA);
         if (electivasActivas > 0) {
         throw new BusinessException("No se puede deshabilitar el programa porque tiene "
         + electivasActivas + " electivas activas asociadas. Desasócialas primero.");
         }
         **/

        programa.setEstado(EstadoPrograma.DESHABILITADO);
        programaRepository.save(programa);

        return new ProgramaDisableResponse(programa.getId(), programa.getNombre(), "Programa deshabilitado");
    }
    /**
     * Lista todos los programas académicos registrados en la base de datos.
     *
     * @return Lista de {@link ProgramaResponse} con la información de cada programa.
     */
    @Override
    public List<ProgramaResponse> listarProgramas() {
        return programaRepository.findAll().stream()
                .map(this::toResponse)
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
                .map(this::toResponse)
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
                .map(this::toResponse)
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
                .map(this::toResponse)
                .toList();
    }

    /**
     * Convierte una entidad {@link Programa} en un {@link ProgramaResponse}.
     *
     * @param programa Entidad programa a convertir.
     * @return {@link ProgramaResponse} con la información de la entidad.
     */
    private ProgramaResponse toResponse(Programa programa) {
        return new ProgramaResponse(
                programa.getId(),
                programa.getCodigo(),
                programa.getNombre(),
                programa.getEstado().getDescripcion(),
                null // mensaje no aplica aquí
        );
    }

}
