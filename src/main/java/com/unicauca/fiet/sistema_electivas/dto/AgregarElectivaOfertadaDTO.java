package com.unicauca.fiet.sistema_electivas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AgregarElectivaOfertadaDTO {
    @NotNull
    private Long electivaId;

    @NotNull
    private Map<Long, Integer> cuposPorPrograma;
    // Ejemplo: {1: 10, 2: 8} -> programaId : cupos
}
