package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper;


import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CambioEstadoValidacionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioDesicionResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
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

    /**
     * Convierte una entidad {@link RespuestasFormulario} a un DTO {@link RespuestaFormularioDesicionResponse}.
     *
     * <p>Se utiliza para devolver el resultado de una decisión manual sobre una respuesta
     * (por ejemplo, incluir o descartar un estudiante con código inválido).</p>
     *
     * @param respuesta la respuesta del formulario procesada
     * @param mensaje mensaje descriptivo del resultado de la decisión
     * @return DTO con la información de la respuesta y el mensaje de estado
     */
    public static RespuestaFormularioDesicionResponse toDecisionResponse(
            RespuestasFormulario respuesta,
            String mensaje
    )   {
        if (respuesta == null) {
            return null;
        }

        return RespuestaFormularioDesicionResponse.builder()
                .id(respuesta.getId())
                .codigoEstudiante(respuesta.getCodigoEstudiante())
                .correoEstudiante(respuesta.getCorreoEstudiante())
                .nombreCompleto(respuesta.getNombreEstudiante() + " " + respuesta.getApellidosEstudiante())
                .estado(respuesta.getEstado().name())
                .mensaje(mensaje)
                .build();
    }



}
