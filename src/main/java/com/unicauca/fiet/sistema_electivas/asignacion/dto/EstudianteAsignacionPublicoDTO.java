package com.unicauca.fiet.sistema_electivas.asignacion.dto;

import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import lombok.Data;

@Data
public class EstudianteAsignacionPublicoDTO {
    private int numero;
    private String codigo;
    private String apellidos;
    private String nombres;
    private String usuario;
    private EstadoAsignacion estado;
}
