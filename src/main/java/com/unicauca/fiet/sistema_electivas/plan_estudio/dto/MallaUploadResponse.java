package com.unicauca.fiet.sistema_electivas.plan_estudio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Respuesta enviada al cliente luego de procesar la malla curricular.
 */
@Getter
@Setter
@AllArgsConstructor
public class MallaUploadResponse {
    private int materiasProcesadas;
    private String mensaje;
}
