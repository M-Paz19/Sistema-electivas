package com.unicauca.fiet.sistema_electivas.periodo_academico.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.ElectivaOfertada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * Repositorio para la entidad {@link ElectivaOfertada}.
 * Proporciona operaciones CRUD y consultas específicas sobre electivas ofertadas.
 */
@Repository
public interface ElectivaOfertadaRepository extends JpaRepository<ElectivaOfertada, Long> {
    /**
     * Verifica si una electiva tiene historial de haber sido ofertada o cerrada.
     * @param electivaId ID de la electiva
     * @return true si existe al menos un registro en estado OFERTADA o CERRADA
     */
    @Query("""
        SELECT CASE WHEN COUNT(eo) > 0 THEN TRUE ELSE FALSE END
        FROM ElectivaOfertada eo
        WHERE eo.electiva.id = :electivaId
          AND (eo.estado = 'CERRADA' OR eo.estado = 'OFERTADA')
    """)
    boolean hasHistorial(@Param("electivaId") Long electivaId);
    /**
     * Obtiene la primera oferta de una electiva en un estado específico.
     * @param electivaId ID de la electiva
     * @param estado Estado de la oferta
     * @return Optional con la primera oferta encontrada
     */
    Optional<ElectivaOfertada> findFirstByElectivaIdAndEstado(Long electivaId, EstadoElectivaOfertada estado);
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
    List<ElectivaOfertada> findByPeriodoId(Long periodoId);

}
