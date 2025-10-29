package com.unicauca.fiet.sistema_electivas.periodo_academico.dto;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoOferta;
import lombok.*;

import java.time.Instant;
import java.util.Map;

/**

 DTO de salida que representa la información detallada de una electiva ofertada.

 <p>Se utiliza en las respuestas HTTP para mostrar información a los administradores

 o usuarios que consultan las electivas ofertadas en un período determinado.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfertaResponse {

    private Long id;
    private Long electivaId;
    private String codigoElectiva;
    private String nombreElectiva;

    private Long periodoId;
    private String nombrePeriodo;

    private EstadoOferta estado;
    private Map<Long, Integer> cuposPorPrograma;

    private Instant fechaCreacion;
    private Instant fechaActualizacion;
}
