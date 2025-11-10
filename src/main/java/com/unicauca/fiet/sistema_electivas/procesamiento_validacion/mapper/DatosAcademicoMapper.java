package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.DatosAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Componente Mapper para convertir entre la entidad DatosAcademico y sus DTOs.
 */
@Component
public class DatosAcademicoMapper {

    private final ModelMapper modelMapper;

    public DatosAcademicoMapper() {
        this.modelMapper = new ModelMapper();
    }

    /**
     * Convierte una entidad DatosAcademico a su DTO de respuesta.
     *
     * @param datos La entidad JPA DatosAcademico.
     * @return El DTO DatosAcademicoResponse.
     */
    public DatosAcademicoResponse toResponse(DatosAcademico datos) {
        // ModelMapper mapeará automáticamente campos como:
        // id, codigoEstudiante, apellidos, nombres, programa,
        // creditosAprobados, periodosMatriculados, promedioCarrera,
        // aprobadas, esNivelado, porcentajeAvance, estadoAptitud

        return modelMapper.map(datos, DatosAcademicoResponse.class);
    }

    /**
     * Convierte una lista de entidades DatosAcademico a una lista de DTOs.
     *
     * @param listaDatos Lista de entidades JPA.
     * @return Lista de DTOs DatosAcademicoResponse.
     */
    public List<DatosAcademicoResponse> toResponseList(List<DatosAcademico> listaDatos) {
        return listaDatos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}