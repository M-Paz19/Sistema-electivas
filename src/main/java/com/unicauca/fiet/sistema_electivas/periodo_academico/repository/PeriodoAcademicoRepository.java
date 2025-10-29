package com.unicauca.fiet.sistema_electivas.periodo_academico.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad {@link PeriodoAcademico}.
 * Proporciona operaciones CRUD y consultas específicas sobre períodos académicos.
 */
@Repository
public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Long> {
    /**
     * Busca un período académico por su semestre.
     *
     * @param semestre Identificador único del semestre (e.g., "2025-2")
     * @return Optional con el período encontrado, o vacío si no existe
     */
    Optional<PeriodoAcademico> findBySemestre(String semestre);

    /**
     * Busca períodos académicos cuyo semestre contenga el texto dado (case-insensitive).
     */
    @Query("""
        SELECT p
        FROM PeriodoAcademico p
        WHERE LOWER(p.semestre) LIKE LOWER(CONCAT('%', :semestreTexto, '%'))
        ORDER BY p.semestre DESC
    """)
    List<PeriodoAcademico> buscarPorSemestre(@Param("semestreTexto") String semestreTexto);

    /**
     * Busca períodos académicos por estado.
     */
    List<PeriodoAcademico> findByEstadoOrderBySemestreDesc(EstadoPeriodoAcademico estado);

    /**
     * Busca períodos académicos cuyo semestre contenga el texto dado (case-insensitive)
     * y además coincidan con el estado especificado.
     */
    @Query("""
        SELECT p
        FROM PeriodoAcademico p
        WHERE LOWER(p.semestre) LIKE LOWER(CONCAT('%', :semestreTexto, '%'))
          AND p.estado = :estado
        ORDER BY p.semestre DESC
    """)
    List<PeriodoAcademico> buscarPorSemestreYEstado(
            @Param("semestreTexto") String semestreTexto,
            @Param("estado") EstadoPeriodoAcademico estado
    );
    /**
     * Mira si existen periodos academicos con algunos estados en particular
     */
    boolean existsByEstadoIn(List<EstadoPeriodoAcademico> estados);
}
