package com.unicauca.fiet.sistema_electivas.departamento.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO que representa la respuesta enviada al cliente
 * con la información completa de un Departamento.
 */
@Getter
@Setter
@AllArgsConstructor
public class DepartamentoResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String estado;
}
