package com.unicauca.fiet.sistema_electivas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO utilizado para devolver la información de un {@code PlanEstudio}.
 */
@Getter
@Setter
@AllArgsConstructor
public class PlanEstudioResponse {
    private Long id;
    private String nombre;
    private String version;
    private String estado;
    private Long programaId;
    private String mensaje;
}
