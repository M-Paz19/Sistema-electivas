package com.unicauca.fiet.sistema_electivas.plan_estudio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO común que representa la información básica de una materia dentro de un plan de estudios.
 *
 * <p>Se utiliza para respuestas estándar donde no se requiere información extendida.</p>
 */
@Getter
@Setter
@AllArgsConstructor
public class PlanMateriaResponse {

    private Long id;

    private Long planEstudiosId;

    private String nombre;

    private Integer semestre;

    private String tipo;

    private Integer creditos;
}
