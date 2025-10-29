package com.unicauca.fiet.sistema_electivas.archivo.enums;

/**
 * Enum que representa los posibles estados de un archivo cargado.
 *
 * <ul>
 *   <li><b>CARGADO:</b> El archivo fue recibido y almacenado correctamente.</li>
 *   <li><b>PROCESADO:</b> El archivo fue leído y sus datos se importaron con éxito.</li>
 *   <li><b>DESECHADO:</b> El archivo fue descartado manual o automáticamente (por error o duplicidad).</li>
 *   <li><b>ERROR:</b> Ocurrió un fallo durante el procesamiento del archivo.</li>
 * </ul>
 */
public enum EstadoArchivo {
    CARGADO("Cargado correctamente"),
    PROCESADO("Procesado exitosamente"),
    DESECHADO("Desechado"),
    ERROR("Error en el procesamiento");

    private final String descripcion;

    EstadoArchivo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
