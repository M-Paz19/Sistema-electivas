package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class CorregirCodigoRequest {

    @NotEmpty(message = "El nuevo código no puede estar vacío")
    private String nuevoCodigo;
}