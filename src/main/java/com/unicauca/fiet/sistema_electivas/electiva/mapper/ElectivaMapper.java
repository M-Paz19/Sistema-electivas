package com.unicauca.fiet.sistema_electivas.electiva.mapper;

import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.electiva.dto.*;
import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;

import java.util.Collections;
import java.util.List;

/**
 * Clase utilitaria encargada de transformar objetos entre las entidades del dominio
 * y los distintos DTOs utilizados en las capas de servicio y presentación.
 *
 * <p>Este mapper sigue una estructura estática (métodos estáticos),
 * ya que no requiere mantener estado ni depender de inyección de dependencias.</p>
 */
public class ElectivaMapper {

    /**
     * Convierte una entidad {@link Electiva} en su representación {@link ElectivaResponseDTO}.
     *
     * @param electiva entidad a convertir
     * @return DTO con los datos de la electiva y su departamento asociado
     */
    public static ElectivaResponseDTO toResponse(Electiva electiva) {
        if (electiva == null) return null;

        return ElectivaResponseDTO.builder()
                .id(electiva.getId())
                .codigo(electiva.getCodigo())
                .nombre(electiva.getNombre())
                .descripcion(electiva.getDescripcion())
                .estado(electiva.getEstado().name()) // Se usa el nombre del enum (BORRADOR, APROBADA, INACTIVA)
                .departamentoId(
                        electiva.getDepartamento() != null ? electiva.getDepartamento().getId() : null
                )
                .departamentoNombre(
                        electiva.getDepartamento() != null ? electiva.getDepartamento().getNombre() : null
                )
                .build();
    }

    /**
     * Convierte un DTO de creación {@link CrearElectivaDTO} en una entidad {@link Electiva}.
     *
     * <p>Este método no asigna el estado ni el ID, ya que esos valores
     * deben ser gestionados por la capa de servicio según la lógica de negocio.</p>
     *
     * @param dto datos provenientes de la solicitud del cliente
     * @param departamento entidad del departamento asociado (ya cargada desde la BD)
     * @return nueva entidad {@link Electiva} lista para ser persistida
     */
    public static Electiva toEntity(CrearElectivaDTO dto, Departamento departamento) {
        Electiva electiva = new Electiva();
        electiva.setCodigo(dto.getCodigo());
        electiva.setNombre(dto.getNombre());
        electiva.setDescripcion(dto.getDescripcion());
        electiva.setDepartamento(departamento);
        return electiva;
    }

    /**
     * Actualiza los campos editables de una entidad {@link Electiva}
     * utilizando los datos provenientes del DTO {@link ActualizarElectivaDTO}.
     *
     * <p>Solo modifica los campos no nulos enviados por el cliente,
     * para evitar sobreescribir valores existentes innecesariamente.</p>
     *
     * @param electiva entidad existente a actualizar
     * @param dto DTO con los nuevos valores
     * @param nuevoDepto (opcional) nuevo departamento si fue modificado
     */
    public static void updateEntity(Electiva electiva, ActualizarElectivaDTO dto, Departamento nuevoDepto) {
        if (dto.getCodigo() != null) electiva.setCodigo(dto.getCodigo());
        if (dto.getNombre() != null) electiva.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) electiva.setDescripcion(dto.getDescripcion());
        if (nuevoDepto != null) electiva.setDepartamento(nuevoDepto);
        // Los programasIds se manejan en la capa de servicio (por relaciones ManyToMany)
    }
}
