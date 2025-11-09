package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper;



import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestaOpcion;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.ElectivaSeleccionadaResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioDesicionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase utilitaria encargada de transformar objetos entre la entidad {@link RespuestasFormulario}
 * y su representación {@link RespuestaFormularioResponse}.
 *
 * <p>Este mapper utiliza únicamente métodos estáticos,
 * ya que no mantiene estado ni depende de inyección de dependencias.</p>
 */
public class RespuestaFormularioMapper {

    /**
     * Convierte una entidad {@link RespuestasFormulario} en su representación {@link RespuestaFormularioResponse}.
     *
     * <p>Incluye el mapeo de las electivas seleccionadas (opciones del formulario)
     * hacia objetos {@link ElectivaSeleccionadaResponse}, que contienen
     * el número de opción y el nombre de la electiva ofertada.</p>
     *
     * @param entidad entidad de tipo {@code RespuestasFormulario}
     * @return DTO con los datos formateados para el cliente
     */
    public static RespuestaFormularioResponse toResponse(RespuestasFormulario entidad) {
        if (entidad == null) return null;

        // Convertir las opciones seleccionadas a DTOs con nombre y número de opción
        List<ElectivaSeleccionadaResponse> electivasSeleccionadas = entidad.getOpciones().stream()
                .filter(op -> op.getOferta() != null && op.getOferta().getElectiva() != null)
                .map(op -> new ElectivaSeleccionadaResponse(
                        op.getOpcionNum(),
                        op.getOferta().getElectiva().getNombre()
                ))
                .collect(Collectors.toList());

        return new RespuestaFormularioResponse(
                entidad.getId(),
                entidad.getCodigoEstudiante(),
                entidad.getCorreoEstudiante(),
                entidad.getNombreEstudiante(),
                entidad.getApellidosEstudiante(),
                entidad.getPrograma() != null ? entidad.getPrograma().getNombre() : null,
                entidad.getPeriodo() != null ? entidad.getPeriodo().getSemestre() : null,
                entidad.getTimestampRespuesta(),
                entidad.getEstado().getDescripcion(),
                electivasSeleccionadas
        );
    }


    /**
     * Convierte una lista de entidades {@link RespuestasFormulario} a una lista de {@link RespuestaFormularioResponse}.
     *
     * @param entidades lista de entidades
     * @return lista de DTOs equivalentes
     */
    public static List<RespuestaFormularioResponse> toResponseList(List<RespuestasFormulario> entidades) {
        return entidades.stream()
                .map(RespuestaFormularioMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static RespuestaFormularioDesicionResponse toRespuestaFormularioResponse(RespuestasFormulario entidad) {
        return RespuestaFormularioDesicionResponse.builder()
                .id(entidad.getId())
                .codigoEstudiante(entidad.getCodigoEstudiante())
                .correoEstudiante(entidad.getCorreoEstudiante())
                .nombreCompleto(entidad.getNombreEstudiante() + " " + entidad.getApellidosEstudiante())
                .estado(entidad.getEstado().name())
                .mensaje("Estado actualizado correctamente.")
                .build();
    }
}
