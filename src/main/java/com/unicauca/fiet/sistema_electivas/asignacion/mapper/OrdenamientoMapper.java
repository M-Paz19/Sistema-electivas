package com.unicauca.fiet.sistema_electivas.asignacion.mapper;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteOrdenamientoResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase encargada de transformar entidades {@link DatosAcademico}
 * en sus correspondientes DTOs de tipo {@link EstudianteOrdenamientoResponse}.
 *
 * <p>Este mapper se utiliza específicamente para las operaciones de ordenamiento
 * de estudiantes aptos, construyendo la respuesta necesaria para los procesos
 * de priorización en electivas.</p>
 */
@Component
public class OrdenamientoMapper {

    /**
     * Convierte una entidad {@link DatosAcademico} en un DTO {@link EstudianteOrdenamientoResponse}.
     *
     * <p>Incluye tanto el mapeo directo de atributos como el cálculo de
     * campos derivados, tales como electivas faltantes y totales requeridas.</p>
     *
     * @param datos entidad que contiene la información académica del estudiante
     * @return DTO con los datos consolidados y listos para ser enviados al cliente
     */
    public EstudianteOrdenamientoResponse toResponse(DatosAcademico datos) {
        if (datos == null) {
            return null; // evita NullPointerException
        }
        EstudianteOrdenamientoResponse dto = new EstudianteOrdenamientoResponse();

        // -----------------------
        //  Mapeo directo 1:1
        // -----------------------
        dto.setId(datos.getId());
        dto.setCodigoEstudiante(datos.getCodigoEstudiante());
        dto.setApellidos(datos.getApellidos());
        dto.setNombres(datos.getNombres());
        dto.setUsuario(datos.getUsuario());
        dto.setPrograma(datos.getPrograma());

        dto.setCreditosAprobados(datos.getCreditosAprobados());
        dto.setPeriodosMatriculados(datos.getPeriodosMatriculados());

        dto.setPromedioCarrera(datos.getPromedioCarrera());
        dto.setPorcentajeAvance(datos.getPorcentajeAvance());

        dto.setAprobadas(datos.getAprobadas());
        dto.setEsNivelado(datos.getEsNivelado());

        dto.setEstadoAptitud(datos.getEstadoAptitud());

        // -----------------------
        //  Campos calculados
        // -----------------------

        // Total de electivas requeridas según el plan de estudios
        Integer totalElectivas = datos.getPlanEstudios().getElectivasRequeridas();
        dto.setDebeVer(totalElectivas);

        // Electivas faltantes = debeVer - aprobadas
        int faltantes = totalElectivas - datos.getAprobadas();
        dto.setFaltan(Math.max(faltantes, 0)); // evita valores negativos

        return dto;
    }

    /**
     * Convierte una lista de entidades {@link DatosAcademico}
     * en una lista de DTOs {@link EstudianteOrdenamientoResponse}.
     *
     * <p>Utiliza la operación de streaming para aplicar la conversión individual
     * definida en {@link #toResponse(DatosAcademico)}.</p>
     *
     * @param lista lista de entidades a transformar
     * @return lista de DTOs generados a partir de la entidad original
     */
    public List<EstudianteOrdenamientoResponse> toResponseList(List<DatosAcademico> lista) {
        return lista.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
