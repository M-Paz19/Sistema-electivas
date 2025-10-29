package com.unicauca.fiet.sistema_electivas.common.exception;

/**
 * Excepción para errores en la integración con la API de Google Forms.
 */
public class GoogleFormsException extends RuntimeException {

    public GoogleFormsException(String message) {
        super(message);
    }

    public GoogleFormsException(String message, Throwable cause) {
        super(message, cause);
    }
}
