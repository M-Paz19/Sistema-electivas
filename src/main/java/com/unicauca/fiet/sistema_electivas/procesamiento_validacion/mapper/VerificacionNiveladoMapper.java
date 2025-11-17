package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.MateriaComparadaDTO;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.VerificacionNiveladoDTO;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper para construir el DTO de Verificación de Nivelado
 * a partir de la entidad DatosAcademico y los resultados de comparación.
 */
@Component
public class VerificacionNiveladoMapper {

    /**
     * Convierte los datos académicos y los resultados de comparación
     * en un DTO de verificación de nivelado.
     *
     * @param datos La entidad DatosAcademico
     * @param comparacion Lista de materias comparadas (plan vs. historia)
     * @param todasAprobadas true si aprobó todas las materias
     * @return DTO VerificacionNiveladoDTO
     */
    public VerificacionNiveladoDTO toDTO(
            DatosAcademico datos,
            List<MateriaComparadaDTO> comparacion,
            boolean todasAprobadas,
            int semestreVerificado
    ) {
        VerificacionNiveladoDTO dto = new VerificacionNiveladoDTO();

        dto.setCodigoEstudiante(datos.getCodigoEstudiante());
        dto.setNombre(datos.getNombres() + " " + datos.getApellidos() + " (" + datos.getUsuario() + ")");
        dto.setPrograma(datos.getPrograma());
        dto.setNivelado(todasAprobadas);
        dto.setComparacionMaterias(comparacion);
        dto.setSemestreVerificado(semestreVerificado);

        // Generar mensaje resumen
        List<String> noAprobadas = comparacion.stream()
                .filter(m -> !m.isAprobada())
                .map(MateriaComparadaDTO::getNombre)
                .toList();

        String mensaje = noAprobadas.isEmpty()
                ? "Todas las materias del plan hasta el semestre " + semestreVerificado + " están aprobadas."
                : "Materias pendientes por aprobar en los semestres 1-" + semestreVerificado + ": " + String.join(", ", noAprobadas);

        dto.setMensajeResumen(mensaje);
        return dto;
    }
}
