package com.unicauca.fiet.sistema_electivas.plan_estudio.repository;

import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanMateria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio para la gestión de entidades {@link PlanMateria}.
 * Extiende de {@link JpaRepository} para operaciones CRUD
 * y define consultas personalizadas relacionadas con las materias de los planes.
 */
public interface PlanMateriaRepository extends JpaRepository<PlanMateria, Long> {
    /**
     * Obtiene todas las materias de un plan de estudios.
     *
     * @param planEstudio plan de estudios
     * @return lista de materias asociadas al plan
     */
    List<PlanMateria> findByPlanEstudios(PlanEstudio planEstudio);

    /**
     * Obtiene las materias de un plan de estudios hasta un semestre específico (inclusive),
     * ordenadas por semestre ascendente y nombre ascendente.
     *
     * @param planEstudioId ID del plan de estudios
     * @param semestre límite superior del semestre (inclusive)
     * @return lista de materias correspondientes
     */
    List<PlanMateria> findByPlanEstudios_IdAndSemestreLessThanEqualOrderBySemestreAscNombreAsc(
            Long planEstudioId,
            int semestre
    );
}
