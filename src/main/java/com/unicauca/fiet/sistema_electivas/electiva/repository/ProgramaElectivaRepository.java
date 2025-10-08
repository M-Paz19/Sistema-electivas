package com.unicauca.fiet.sistema_electivas.electiva.repository;

import com.unicauca.fiet.sistema_electivas.electiva.model.ProgramaElectiva;
import com.unicauca.fiet.sistema_electivas.electiva.model.ProgramaElectivaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramaElectivaRepository extends JpaRepository<ProgramaElectiva, ProgramaElectivaId> {

    // Opcional: métodos personalizados, por ejemplo:
    boolean existsByProgramaIdAndElectivaId(Long programaId, Long electivaId);

    @Modifying
    @Query("DELETE FROM ProgramaElectiva pe WHERE pe.electiva.id = :electivaId")
    void deleteByElectivaId(@Param("electivaId") Long electivaId);

    @Query("SELECT pe FROM ProgramaElectiva pe WHERE pe.electiva.id = :electivaId")
    List<ProgramaElectiva> findByElectivaId(@Param("electivaId") Long electivaId);

}
