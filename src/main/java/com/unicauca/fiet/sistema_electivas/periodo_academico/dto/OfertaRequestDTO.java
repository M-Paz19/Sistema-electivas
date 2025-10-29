package com.unicauca.fiet.sistema_electivas.periodo_academico.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
/**

 DTO utilizado para agregar una nueva electiva ofertada dentro de un período académico.

 <p>Define la electiva base y la asignación de cupos por programa académico.</p>

 */
@Getter
@Setter
public class OfertaRequestDTO {

    @NotNull(message = "El ID de la electiva es obligatorio")
    private Long electivaId;

    @NotNull(message = "Debe especificar los cupos por programa")
    private Map<Long, Integer> cuposPorPrograma;// Ejemplo: {1: 10, 2: 8} -> programaId : cupos
}
