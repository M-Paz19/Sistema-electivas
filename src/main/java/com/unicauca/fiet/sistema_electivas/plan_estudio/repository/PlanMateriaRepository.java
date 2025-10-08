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
    List<PlanMateria> findByPlanEstudios_Id(Long planEstudioId);
}
