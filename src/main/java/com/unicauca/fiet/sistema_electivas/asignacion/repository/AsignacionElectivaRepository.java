package com.unicauca.fiet.sistema_electivas.asignacion.repository;


import com.unicauca.fiet.sistema_electivas.asignacion.model.AsignacionElectiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsignacionElectivaRepository extends JpaRepository<AsignacionElectiva, Long> {

    /**
     * Cuenta cuántas asignaciones con estado ASIGNADA existen para una oferta determinada,
     * **exclusivamente para estudiantes pertenecientes a un programa específico**.
     *
     * Lógica:
     * - Toma todas las asignaciones (AsignacionElectiva) asociadas a la oferta.
     * - Filtra solo las que están con estado = ASIGNADA.
     * - Además, toma solo asignaciones cuyo código de estudiante pertenezca a estudiantes
     *   cuya respuesta al formulario corresponde al programa indicado.
     *
     * Esto permite validar cuántos cupos se han asignado POR PROGRAMA dentro de una oferta.
     *
     * @param ofertaId  ID de la oferta de electiva.
     * @param programaId ID del programa académico.
     * @return Número de asignaciones ASIGNADAS para ese programa dentro de la oferta.
     */
    @Query("""
        SELECT COUNT(a) 
        FROM AsignacionElectiva a
        WHERE a.oferta.id = :ofertaId
          AND a.estadoAsignacion = 'ASIGNADA'
          AND a.estudianteCodigo IN (
              SELECT r.codigoEstudiante 
              FROM RespuestasFormulario r
              WHERE r.programa.id = :programaId
          )
    """)
    int countAsignadasByOfertaAndPrograma(Long ofertaId, Long programaId);

    @Query("""
    SELECT COUNT(a)
    FROM AsignacionElectiva a
    WHERE a.oferta.id = :ofertaId
      AND a.estadoAsignacion = 'LISTA_ESPERA'
    """)
    int countListaEsperaByOferta(Long ofertaId);

    Optional<AsignacionElectiva> findByEstudianteCodigoAndOfertaIdAndNumeroOpcion(
            String estudianteCodigo,
            Long ofertaId,
            Integer numeroOpcion
    );

}