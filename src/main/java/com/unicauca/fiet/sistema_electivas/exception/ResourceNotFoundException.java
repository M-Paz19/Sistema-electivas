package com.unicauca.fiet.sistema_electivas.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
