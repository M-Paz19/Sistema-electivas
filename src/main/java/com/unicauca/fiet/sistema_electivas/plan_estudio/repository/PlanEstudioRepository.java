package com.unicauca.fiet.sistema_electivas.plan_estudio.repository;

import com.unicauca.fiet.sistema_electivas.plan_estudio.enums.EstadoPlanEstudio;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
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

    /**
     * Verifica si ya existe un plan de estudio en un programa para un año específico.
     *
     * @param programa programa académico
     * @param anioInicio año de inicio del plan
     * @return true si existe un plan con ese año en el programa, false en caso contrario
     */
    boolean existsByProgramaAndAnioInicio(Programa programa, Integer anioInicio);

    /**
     * Busca un Plan de Estudio basado en el nombre de su Programa asociado.
     * Asume que el nombre del programa es único.
     */
    Optional<PlanEstudio> findByPrograma_Nombre(String nombrePrograma);

    /**
     * Lista los planes de estudio filtrando por estado.
     *
     * @param estado   estado del plan de estudio
     * @return lista filtrada de planes
     */
    List<PlanEstudio> findByEstado(EstadoPlanEstudio estado);


}
