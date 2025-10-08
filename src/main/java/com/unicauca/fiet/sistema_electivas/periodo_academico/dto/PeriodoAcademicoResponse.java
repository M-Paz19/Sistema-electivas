package com.unicauca.fiet.sistema_electivas.periodo_academico.dto;


import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO utilizado para devolver la información de un {@code PeriodoAcademico}.
 */
@Getter
@Setter
@AllArgsConstructor
public class PeriodoAcademicoResponse {

    private Long id;
    private String semestre;
    private Instant fechaApertura;
    private Instant fechaCierre;
    private String estado; // Se puede guardar como String para mostrar directamente
    private String mensaje; // Mensaje opcional para notificaciones

    // --- Método de fábrica para crear el DTO desde la entidad ---
    public static PeriodoAcademicoResponse fromEntity(PeriodoAcademico entity, String mensaje) {
        return new PeriodoAcademicoResponse(
                entity.getId(),
                entity.getSemestre(),
                entity.getFechaApertura(),
                entity.getFechaCierre(),
                entity.getEstado().getDescripcion(), // convierte el enum a String
                mensaje
        );
    }
}
