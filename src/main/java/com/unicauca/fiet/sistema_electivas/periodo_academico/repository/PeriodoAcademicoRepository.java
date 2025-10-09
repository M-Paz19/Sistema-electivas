package com.unicauca.fiet.sistema_electivas.periodo_academico.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad {@link PeriodoAcademico}.
 * Proporciona operaciones CRUD y consultas específicas sobre períodos académicos.
 */
public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Long> {
    /**
     * Busca un período académico por su semestre.
     *
     * @param semestre Identificador único del semestre (e.g., "2025-2")
     * @return Optional con el período encontrado, o vacío si no existe
     */
    Optional<PeriodoAcademico> findBySemestre(String semestre);

    /**
     * Busca períodos académicos cuyo semestre contenga el texto dado (case-insensitive)
     * y opcionalmente filtrando por estado.
     *
     * @param semestreTexto Texto parcial del semestre (ej: "2025")
     * @param estado Estado del período (opcional, puede ser null para no filtrar)
     * @return Lista de períodos que cumplen los criterios de búsqueda
     */
    @Query("""
        SELECT p
        FROM PeriodoAcademico p
        WHERE (:semestreTexto IS NULL OR LOWER(p.semestre) LIKE LOWER(CONCAT('%', :semestreTexto, '%')))
          AND (:estado IS NULL OR p.estado = :estado)
        ORDER BY p.semestre DESC
    """)
    List<PeriodoAcademico> buscarPorSemestreYEstado(
            @Param("semestreTexto") String semestreTexto,
            @Param("estado") EstadoPeriodoAcademico estado
    );
}
