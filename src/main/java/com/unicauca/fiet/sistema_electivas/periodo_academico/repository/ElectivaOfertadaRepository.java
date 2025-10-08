package com.unicauca.fiet.sistema_electivas.periodo_academico.repository;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.ElectivaOfertada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ElectivaOfertadaRepository extends JpaRepository<ElectivaOfertada, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(eo) > 0 THEN TRUE ELSE FALSE END
        FROM ElectivaOfertada eo
        WHERE eo.electiva.id = :electivaId
          AND (eo.estado = 'CERRADA' OR eo.estado = 'OFERTADA')
    """)
    boolean hasHistorial(@Param("electivaId") Long electivaId);

    Optional<ElectivaOfertada> findFirstByElectivaIdAndEstado(Long electivaId, EstadoElectivaOfertada estado);

    boolean existsByElectivaIdAndPeriodoId(Long electivaId, Long periodoId);

}
