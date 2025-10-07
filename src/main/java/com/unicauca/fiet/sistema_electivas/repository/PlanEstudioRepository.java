package com.unicauca.fiet.sistema_electivas.repository;

import com.unicauca.fiet.sistema_electivas.enums.EstadoPlanEstudio;
import com.unicauca.fiet.sistema_electivas.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.model.Programa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades {@link PlanEstudio}.
 * Extiende de {@link JpaRepository} para operaciones CRUD
 * y define consultas personalizadas relacionadas con planes de estudio.
 */
public interface PlanEstudioRepository extends JpaRepository<PlanEstudio, Long> {

    /**
     * Busca un plan de estudios por nombre dentro de un programa específico.
     *
     * @param nombre nombre del plan
     * @param programa programa asociado
     * @return un {@link Optional} con el plan si existe, o vacío en caso contrario
     */
    Optional<PlanEstudio> findByNombreAndPrograma(String nombre, Programa programa);

    /**
     * Lista todos los planes de estudio asociados a un programa.
     *
     * @param programa programa académico
     * @return lista de planes de estudio
     */
    List<PlanEstudio> findByPrograma(Programa programa);

    /**
     * Lista los planes de estudio de un programa filtrando por estado.
     *
     * @param programa programa académico
     * @param estado   estado del plan de estudio
     * @return lista filtrada de planes
     */
    List<PlanEstudio> findByProgramaAndEstado(Programa programa, EstadoPlanEstudio estado);

    /**
     * Cuenta la cantidad de planes para un programa y estado especifico
     *
     * @param programa programa académico
     * @param estado   estado del plan de estudio
     * @return lista filtrada de planes
     */
    long countByProgramaAndEstado(Programa programa, EstadoPlanEstudio estado);

}
