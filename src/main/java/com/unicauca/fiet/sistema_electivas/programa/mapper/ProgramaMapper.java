package com.unicauca.fiet.sistema_electivas.programa.mapper;


import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaRequest;
import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaResponse;
import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaUpdateRequest;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma;

/**
 * Clase utilitaria encargada de transformar objetos entre la entidad {@link Programa}
 * y los distintos DTOs utilizados en las capas de servicio y presentación.
 *
 * <p>Este mapper utiliza únicamente métodos estáticos,
 * ya que no mantiene estado ni depende de inyección de dependencias.</p>
 */
public class ProgramaMapper {

    /**
     * Convierte una entidad {@link Programa} en su representación {@link ProgramaResponse}.
     *
     * @param programa entidad a convertir
     * @return DTO con los datos del programa
     */
    public static ProgramaResponse toResponse(Programa programa) {
        if (programa == null) return null;

        return new ProgramaResponse(
                programa.getId(),
                programa.getCodigo(),
                programa.getNombre(),
                programa.getEstado().name(),
                programa.getFechaCreacion()
        );
    }

    /**
     * Convierte un DTO de creación {@link ProgramaRequest} en una entidad {@link Programa}.
     *
     * <p>Este método no asigna el ID ni la fecha de creación, ya que esos valores
     * son gestionados automáticamente por la base de datos o Hibernate.</p>
     *
     * @param dto datos provenientes de la solicitud del cliente
     * @return nueva entidad {@link Programa} lista para ser persistida
     */
    public static Programa toEntity(ProgramaRequest dto) {
        Programa programa = new Programa();
        programa.setCodigo(dto.getCodigo());
        programa.setNombre(dto.getNombre());
        programa.setEstado(EstadoPrograma.PENDIENTE_PLAN); // o el estado inicial por defecto
        return programa;
    }

    /**
     * Actualiza los campos editables de una entidad {@link Programa}
     * utilizando los datos del DTO {@link ProgramaUpdateRequest}.
     *
     * <p>Solo modifica los campos no nulos enviados por el cliente,
     * para evitar sobrescribir valores existentes innecesariamente.</p>
     *
     * @param programa entidad existente a actualizar
     * @param dto DTO con los nuevos valores
     */
    public static void updateEntity(Programa programa, ProgramaUpdateRequest dto) {
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            programa.setNombre(dto.getNombre());
        }
        // No se permite cambiar el código ni el estado aquí.
    }
}
