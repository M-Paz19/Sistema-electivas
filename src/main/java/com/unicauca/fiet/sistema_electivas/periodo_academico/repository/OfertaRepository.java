package com.unicauca.fiet.sistema_electivas.periodo_academico.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoOferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * Repositorio para la entidad {@link Oferta}.
 * Proporciona operaciones CRUD y consultas específicas sobre electivas ofertadas.
 */
@Repository
public interface OfertaRepository extends JpaRepository<Oferta, Long> {
    /**
     * Verifica si una electiva tiene historial de haber sido ofertada o cerrada.
     * @param electivaId ID de la electiva
     * @return true si existe al menos un registro en estado OFERTADA o CERRADA
     */
    @Query("""
        SELECT CASE WHEN COUNT(eo) > 0 THEN TRUE ELSE FALSE END
        FROM Oferta eo
        WHERE eo.electiva.id = :electivaId
          AND (eo.estado = 'CERRADA' OR eo.estado = 'OFERTADA' OR eo.estado = 'EN_CURSO')
    """)
    boolean hasHistorial(@Param("electivaId") Long electivaId);
    /**
     * Obtiene la primera oferta de una electiva en un estado específico.
     * @param electivaId ID de la electiva
     * @param estados Estados de la oferta
     * @return Optional con la primera oferta encontrada
     */
    Optional<Oferta> findFirstByElectivaIdAndEstadoIn(Long electivaId, List<EstadoOferta> estados);

    /**
     * Verifica si una electiva ya está incluida en la oferta de un período.
     * @param electivaId ID de la electiva
     * @param periodoId ID del período académico
     * @return true si ya existe una oferta para ese período
     */
    boolean existsByElectivaIdAndPeriodoId(Long electivaId, Long periodoId);
    /**
     * Obtiene todas las electivas ofertadas asociadas a un período académico específico.
     *
     * @param periodoId ID del período académico
     * @return Lista de electivas ofertadas correspondientes
     */
    List<Oferta> findByPeriodoId(Long periodoId);
    /**
     * Obtiene la confirmacion de tener oferta validad
     * osea mas de una electiva ofertada en el periodo academico
     *
     * @param periodoId ID del período académico
     * @return Un boleeano para saber si el periodo academico tiene mas de una electiva ofertada registradas
     */
    @Query("SELECT COUNT(eo) > 0 FROM Oferta eo WHERE eo.periodo.id = :periodoId AND eo.estado = 'OFERTADA'")
    boolean hasOfertaValida(@Param("periodoId") Long periodoId);

    /**
     * Retorna todas las electivas ofertadas asociadas a un período.
     *
     * @param periodoId ID del período académico
     * @return lista de electivas ofertadas
     */
    List<Oferta> findByPeriodo_Id(Long periodoId);
    /**
     * Busca una oferta asociada a una electiva por nombre (ignorando mayúsculas/minúsculas)
     * y un período académico específico.
     *
     * @param nombreElectiva nombre de la electiva (campo de Electiva)
     * @param periodo        período académico al que pertenece la oferta
     * @return una oferta coincidente o vacío si no existe
     */
    @Query("""
        SELECT o FROM Oferta o
        WHERE LOWER(o.electiva.nombre) = LOWER(:nombreElectiva)
        AND o.periodo = :periodo
    """)
    Optional<Oferta> findByElectivaNombreIgnoreCaseAndPeriodo(@Param("nombreElectiva") String nombreElectiva,
                                                              @Param("periodo") PeriodoAcademico periodo);
}
