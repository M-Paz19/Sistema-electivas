package com.unicauca.fiet.sistema_electivas.common.exception;

/**
 * Excepción que representa un intento de realizar una operación no permitida
 * debido al estado actual de una entidad del dominio.
 *
 * <p>Ejemplos de uso:
 * <ul>
 *   <li>Intentar desactivar un plan que no está en estado ACTIVO o CONFIGURACION_PENDIENTE.</li>
 *   <li>Intentar modificar una entidad que ya fue cerrada o archivada.</li>
 * </ul>
 */
public class InvalidStateException extends RuntimeException {

    /**
     * Crea una nueva excepción de estado inválido con un mensaje descriptivo.
     *
     * @param message Mensaje que describe el error de estado.
     */
    public InvalidStateException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción de estado inválido con un mensaje y una causa original.
     *
     * @param message Mensaje que describe el error de estado.
     * @param cause   La excepción original que causó este error.
     */
    public InvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
