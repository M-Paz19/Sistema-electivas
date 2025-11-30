package com.unicauca.fiet.sistema_electivas.electiva.repository;

import com.unicauca.fiet.sistema_electivas.electiva.enums.EstadoElectiva;
import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.electiva.model.ProgramaElectiva;
import com.unicauca.fiet.sistema_electivas.electiva.model.ProgramaElectivaId;
import com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramaElectivaRepository extends JpaRepository<ProgramaElectiva, ProgramaElectivaId> {

    /**
     * Verifica si existe una relación entre un programa y una electiva.
     *
     * @param programaId ID del programa.
     * @param electivaId ID de la electiva.
     * @return true si la relación existe.
     */
    boolean existsByProgramaIdAndElectivaId(Long programaId, Long electivaId);

    /**
     * Elimina todas las relaciones entre un programa y una electiva,
     * dado el ID de la electiva.
     *
     * <p>Usado especialmente cuando se elimina o desactiva una electiva.</p>
     *
     * @param electivaId ID de la electiva.
     */
    @Modifying
    @Query("DELETE FROM ProgramaElectiva pe WHERE pe.electiva.id = :electivaId")
    void deleteByElectivaId(@Param("electivaId") Long electivaId);

    /**
     * Obtiene todas las relaciones programa–electiva asociadas a una electiva.
     *
     * @param electivaId ID de la electiva.
     * @return lista de entidades ProgramaElectiva vinculadas a la electiva.
     */
    @Query("SELECT pe FROM ProgramaElectiva pe WHERE pe.electiva.id = :electivaId")
    List<ProgramaElectiva> findByElectivaId(@Param("electivaId") Long electivaId);

    @Query("""
    SELECT COUNT(pe)
    FROM ProgramaElectiva pe
    WHERE pe.programa.id = :programaId
      AND pe.electiva.estado <> :estado
    """)
    int countElectivasActivasByProgramaId(
            @Param("programaId") Long programaId,
            @Param("estado") EstadoElectiva estado
    );

    /**
     * Obtiene todos los programas asociados a una electiva.
     *
     * @param electivaId ID de la electiva.
     * @return lista de programas que tienen relación con la electiva.
     */
    @Query("""
        SELECT pe.programa
        FROM ProgramaElectiva pe
        WHERE pe.electiva.id = :electivaId
    """)
    List<Programa> findProgramasByElectivaId(@Param("electivaId") Long electivaId);

    /**
     * Retorna todas las relaciones {@link ProgramaElectiva} donde el programa asociado
     * se encuentra en estado {@link EstadoPrograma#APROBADO}.
     *
     * <p>Este método utiliza <code>JOIN FETCH</code> para cargar de manera anticipada
     * tanto el {@link Programa} como la {@link Electiva}, evitando el problema de
     * N+1 queries y optimizando el acceso a los datos relacionados.</p>
     *
     * @return una lista de relaciones programa–electiva cuyo programa está aprobado.
     */
    @Query("""
    SELECT pe
    FROM ProgramaElectiva pe
    JOIN FETCH pe.programa p
    JOIN FETCH pe.electiva e
    WHERE p.estado = com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma.APROBADO
""")
    List<ProgramaElectiva> findAllWithProgramaAprobadoAndElectiva();

}
