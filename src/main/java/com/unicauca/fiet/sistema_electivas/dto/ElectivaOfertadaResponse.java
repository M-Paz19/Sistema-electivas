package com.unicauca.fiet.sistema_electivas.dto;

import com.unicauca.fiet.sistema_electivas.enums.EstadoElectivaOfertada;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectivaOfertadaResponse {

    private Long id;
    private Long electivaId;
    private String codigoElectiva;
    private String nombreElectiva;

    private Long periodoId;
    private String nombrePeriodo;

    private EstadoElectivaOfertada estado;
    private Map<Long, Integer> cuposPorPrograma;

    private Instant fechaCreacion;
    private Instant fechaActualizacion;
}
