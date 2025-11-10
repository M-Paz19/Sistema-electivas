package com.unicauca.fiet.sistema_electivas.archivo.enums;

/**
 * Enum que representa los diferentes tipos de archivos
 * que pueden cargarse en el sistema académico.
 *
 * <ul>
 *   <li><b>RESPUESTAS_FORMULARIO:</b> Archivo proveniente de un formulario de selección de electivas.</li>
 *   <li><b>LOTES_CODIGOS:</b> Archivo txt con los codigos perteneciente a un lote para obtener información en SIMCA.</li>
 *   <li><b>DATOS_ACADEMICOS:</b> Archivo con información académica de los estudiantes (códigos, programas, etc.).</li>
 *   <li><b>ARCHIVO_ASIGNACION:</b> Archivo con resultados de asignación de electivas a estudiantes.</li>
 *   <li><b>LISTAS:</b> Listados consolidados o informes generados a partir de asignaciones.</li>
 * </ul>
 */
public enum TipoArchivo {
    RESPUESTAS_FORMULARIO("Respuestas de formulario"),
    LOTES_CODIGOS("Lotes codigos estudiantiles para SIMCA"),
    DATOS_ACADEMICOS("Datos académicos"),
    ARCHIVO_ASIGNACION("Archivo de asignación de electivas"),
    LISTAS("Listas consolidadas");


    private final String descripcion;

    TipoArchivo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
