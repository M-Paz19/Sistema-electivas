package com.unicauca.fiet.sistema_electivas.asignacion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * DTO que representa la estructura principal del reporte por departamento.
 *
 * <p>Contiene la información básica del departamento junto con el listado
 * de todas las ofertas de electivas que pertenecen a dicho departamento
 * dentro de un período académico.</p>
 *
 * <p>Este DTO es la raíz del reporte generado para consulta administrativa,
 * recopilando de forma jerárquica los datos del departamento, sus ofertas
 * y los estudiantes asociados a cada una.</p>
 */
@Data
public class DepartamentoReporteDTO {

    /** Identificador único del departamento. */
    private Long id;

    /** Código institucional del departamento. */
    private String codigo;

    /** Nombre oficial del departamento. */
    private String nombre;

    /**
     * Lista de ofertas de electivas pertenecientes al departamento.
     *
     * <p>Cada oferta incluye información sobre la electiva, los programas
     * relacionados y los estudiantes asignados o en espera.</p>
     */
    private List<OfertaReporteDTO> ofertas;

    /**
     * Constructor auxiliar utilizado al generar el reporte,
     * permitiendo inicializar los campos principales del departamento.
     */
    public DepartamentoReporteDTO(Long id, @NotNull String codigo, @NotNull String nombre) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
    }
}
