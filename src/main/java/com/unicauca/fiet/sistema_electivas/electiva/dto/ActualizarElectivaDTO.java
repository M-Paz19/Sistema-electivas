package com.unicauca.fiet.sistema_electivas.electiva.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
/**
 * DTO usado para actualizar los datos de una electiva existente.
 *
 * Permite modificar dependiendo del estado
 */
@Getter
@Setter
public class ActualizarElectivaDTO {
    private String codigo; // Solo editable si BORRADOR o APROBADA sin historial
    private String nombre; // Solo editable si BORRADOR o APROBADA sin historial
    private String descripcion; // Solo editable si BORRADOR o APROBADA sin historial
    private Long departamentoId; // Solo editable si BORRADOR o APROBADA sin historial
    private List<Long> programasIds; // Siempre editable, incluso si tiene historial
}
