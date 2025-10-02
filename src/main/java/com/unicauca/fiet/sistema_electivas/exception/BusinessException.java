package com.unicauca.fiet.sistema_electivas.exception;

/**
 * Excepción que representa un error de negocio en el sistema.
 *
 * <p>Se utiliza para validar reglas específicas de dominio, por ejemplo:
 * <ul>
 *   <li>Intentar deshabilitar un programa con electivas activas.</li>
 *   <li>Realizar una operación no permitida en el estado actual de la entidad.</li>
 * </ul>
 */
public class BusinessException extends RuntimeException {

    /**
     * Crea una nueva excepción de negocio con un mensaje descriptivo.
     *
     * @param message Mensaje que describe el error de negocio.
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción de negocio con un mensaje y una causa original.
     *
     * @param message Mensaje que describe el error de negocio.
     * @param cause   La excepción original que causó este error.
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
