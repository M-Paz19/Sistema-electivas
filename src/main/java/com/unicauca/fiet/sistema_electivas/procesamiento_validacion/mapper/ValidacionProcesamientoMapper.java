package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper;


import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CambioEstadoValidacionResponse;
/**
 * Clase utilitaria para convertir entidades del dominio de validación y procesamiento
 * en objetos de respuesta (DTOs) adecuados para la capa de presentación.
 *
 * <p>Centraliza la lógica de mapeo para mantener el código de servicio más limpio
 * y evitar duplicación de transformaciones comunes.</p>
 */
public class ValidacionProcesamientoMapper {
    /**
     * Constructor privado para evitar la instanciación de esta clase utilitaria.
     */
    private ValidacionProcesamientoMapper() {
        // Evita que se creen instancias de esta clase
    }

    /**
     * Convierte un objeto {@link PeriodoAcademico} a un DTO {@link CambioEstadoValidacionResponse}.
     *
     * <p>Este método se utiliza principalmente cuando se actualiza el estado de un
     * período académico durante procesos automáticos como el filtrado de duplicados
     * o la validación de respuestas.</p>
     *
     * @param periodo el período académico cuyo estado ha cambiado
     * @param mensaje un mensaje descriptivo del resultado de la operación realizada
     * @return un objeto {@link CambioEstadoValidacionResponse} con los datos del nuevo estado;
     *         o {@code null} si el parámetro {@code periodo} es nulo
     */
    public static CambioEstadoValidacionResponse toCambioEstadoResponse(
            PeriodoAcademico periodo,
            String mensaje
    ) {
        if (periodo == null) {
            return null;
        }

        return CambioEstadoValidacionResponse.builder()
                .periodoId(periodo.getId())
                .semestre(periodo.getSemestre())
                .nuevoEstado(periodo.getEstado().getDescripcion())
                .mensaje(mensaje)
                .build();
    }
}
