package com.unicauca.fiet.sistema_electivas.periodo_academico.mapper;

import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.CambioEstadoResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.CrearPeriodoAcademicoDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.PeriodoAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;

/**
 * Clase utilitaria encargada de transformar objetos entre la entidad {@link PeriodoAcademico}
 * y sus correspondientes DTOs.
 *
 * <p>Este mapper utiliza métodos estáticos porque no requiere mantener estado ni inyección de dependencias.</p>
 */
public class PeriodoAcademicoMapper {

    /**
     * Convierte un DTO {@link CrearPeriodoAcademicoDTO} en una entidad {@link PeriodoAcademico}.
     *
     * <p>Este método se utiliza al crear un nuevo período académico desde el controlador o servicio.</p>
     *
     * @param dto DTO con los datos de creación del período
     * @return entidad {@link PeriodoAcademico} lista para ser persistida
     */
    public static PeriodoAcademico toEntity(CrearPeriodoAcademicoDTO dto) {
        if (dto == null) return null;

        PeriodoAcademico periodo = new PeriodoAcademico();
        periodo.setSemestre(dto.getSemestre());
        periodo.setFechaApertura(dto.getFechaApertura());
        periodo.setFechaCierre(dto.getFechaCierre());
        periodo.setEstado(EstadoPeriodoAcademico.CONFIGURACION); // Estado inicial por defecto
        return periodo;
    }

    /**
     * Convierte una entidad {@link PeriodoAcademico} en su representación {@link PeriodoAcademicoResponse}.
     *
     * <p>Este método es utilizado para devolver información a la capa de presentación.</p>
     *
     * @param periodo entidad a convertir
     * @return DTO con los datos del período académico
     */
    public static PeriodoAcademicoResponse toResponse(PeriodoAcademico periodo) {
        if (periodo == null) return null;

        return new PeriodoAcademicoResponse(
                periodo.getId(),
                periodo.getSemestre(),
                periodo.getFechaApertura(),
                periodo.getFechaCierre(),
                periodo.getEstado().name(),
                periodo.getNumeroOpcionesFormulario(),
                periodo.getUrlFormulario()
        );
    }
    /**
     * Convierte un {@link PeriodoAcademico} en un {@link CambioEstadoResponse}
     * luego de una operación de cambio de estado (por ejemplo, apertura o cierre del período).
     *
     * @param periodo Entidad del período académico cuyo estado cambió.
     * @param mensaje Mensaje personalizado de resultado (por ejemplo, éxito o advertencia).
     * @return DTO {@link CambioEstadoResponse} con el nuevo estado y mensaje.
     */
    public static CambioEstadoResponse toCambioEstadoResponse(PeriodoAcademico periodo, String mensaje) {
        return new CambioEstadoResponse(
                periodo.getId(),
                periodo.getSemestre(),
                periodo.getEstado().getDescripcion(),
                mensaje,
                periodo.getUrlFormulario()
        );
    }


}
