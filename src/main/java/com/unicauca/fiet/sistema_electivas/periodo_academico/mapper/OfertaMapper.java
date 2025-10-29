package com.unicauca.fiet.sistema_electivas.periodo_academico.mapper;

import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.OfertaRequestDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.OfertaResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoOferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;

import java.time.Instant;

/**
 * Clase utilitaria encargada de transformar objetos entre la entidad {@link Oferta}
 * y sus correspondientes DTOs.
 *
 * <p>Permite construir y transformar electivas ofertadas desde y hacia sus representaciones
 * utilizadas en las capas de aplicación y presentación.</p>
 */
public class OfertaMapper {

    /**
     * Convierte un DTO {@link OfertaRequestDTO} en una entidad {@link Oferta}.
     *
     * <p>Este método debe complementarse en el servicio con la asignación de las relaciones
     * a {@link Electiva} y {@link PeriodoAcademico}, ya que el DTO solo contiene los IDs.</p>
     *
     * @param dto datos de la nueva electiva ofertada
     * @param electiva entidad {@link Electiva} asociada
     * @param periodo entidad {@link PeriodoAcademico} asociada
     * @return entidad {@link Oferta} lista para persistir
     */
    public static Oferta toEntity(OfertaRequestDTO dto, Electiva electiva, PeriodoAcademico periodo) {
        if (dto == null || electiva == null || periodo == null) return null;

        Oferta ofertada = new Oferta();
        ofertada.setElectiva(electiva);
        ofertada.setPeriodo(periodo);
        ofertada.setCuposPorPrograma(dto.getCuposPorPrograma());
        ofertada.setEstado(EstadoOferta.OFERTADA);
        ofertada.setFechaCreacion(Instant.now());
        ofertada.setFechaActualizacion(Instant.now());
        return ofertada;
    }

    /**
     * Convierte una entidad {@link Oferta} en su representación {@link OfertaResponse}.
     *
     * @param ofertada entidad a convertir
     * @return DTO con la información detallada de la electiva ofertada
     */
    public static OfertaResponse toResponse(Oferta ofertada) {
        if (ofertada == null) return null;

        return OfertaResponse.builder()
                .id(ofertada.getId())
                .electivaId(ofertada.getElectiva() != null ? ofertada.getElectiva().getId() : null)
                .codigoElectiva(ofertada.getElectiva() != null ? ofertada.getElectiva().getCodigo() : null)
                .nombreElectiva(ofertada.getElectiva() != null ? ofertada.getElectiva().getNombre() : null)
                .periodoId(ofertada.getPeriodo() != null ? ofertada.getPeriodo().getId() : null)
                .nombrePeriodo(ofertada.getPeriodo() != null ? ofertada.getPeriodo().getSemestre() : null)
                .estado(ofertada.getEstado())
                .cuposPorPrograma(ofertada.getCuposPorPrograma())
                .fechaCreacion(ofertada.getFechaCreacion())
                .fechaActualizacion(ofertada.getFechaActualizacion())
                .build();
    }
}
