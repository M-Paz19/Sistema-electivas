package com.unicauca.fiet.sistema_electivas.periodo_academico.mapper;

import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.AgregarElectivaOfertadaDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.ElectivaOfertadaResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.ElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;

import java.time.Instant;

/**
 * Clase utilitaria encargada de transformar objetos entre la entidad {@link ElectivaOfertada}
 * y sus correspondientes DTOs.
 *
 * <p>Permite construir y transformar electivas ofertadas desde y hacia sus representaciones
 * utilizadas en las capas de aplicación y presentación.</p>
 */
public class ElectivaOfertadaMapper {

    /**
     * Convierte un DTO {@link AgregarElectivaOfertadaDTO} en una entidad {@link ElectivaOfertada}.
     *
     * <p>Este método debe complementarse en el servicio con la asignación de las relaciones
     * a {@link Electiva} y {@link PeriodoAcademico}, ya que el DTO solo contiene los IDs.</p>
     *
     * @param dto datos de la nueva electiva ofertada
     * @param electiva entidad {@link Electiva} asociada
     * @param periodo entidad {@link PeriodoAcademico} asociada
     * @return entidad {@link ElectivaOfertada} lista para persistir
     */
    public static ElectivaOfertada toEntity(AgregarElectivaOfertadaDTO dto, Electiva electiva, PeriodoAcademico periodo) {
        if (dto == null || electiva == null || periodo == null) return null;

        ElectivaOfertada ofertada = new ElectivaOfertada();
        ofertada.setElectiva(electiva);
        ofertada.setPeriodo(periodo);
        ofertada.setCuposPorPrograma(dto.getCuposPorPrograma());
        ofertada.setEstado(EstadoElectivaOfertada.OFERTADA);
        ofertada.setFechaCreacion(Instant.now());
        ofertada.setFechaActualizacion(Instant.now());
        return ofertada;
    }

    /**
     * Convierte una entidad {@link ElectivaOfertada} en su representación {@link ElectivaOfertadaResponse}.
     *
     * @param ofertada entidad a convertir
     * @return DTO con la información detallada de la electiva ofertada
     */
    public static ElectivaOfertadaResponse toResponse(ElectivaOfertada ofertada) {
        if (ofertada == null) return null;

        return ElectivaOfertadaResponse.builder()
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
