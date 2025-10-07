package com.unicauca.fiet.sistema_electivas.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa la respuesta enviada al cliente tras crear o consultar una electiva.
 * 
 * <p>Incluye los datos básicos de la electiva sin exponer la entidad interna.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectivaResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String estado;
    private Long departamentoId;
    private String departamentoNombre;
    private String mensaje;
}
