package com.unicauca.fiet.sistema_electivas.repository;

import com.unicauca.fiet.sistema_electivas.model.PlanMateria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio para PlanMateria.
 */
public interface PlanMateriaRepository extends JpaRepository<PlanMateria, Long> {
    List<PlanMateria> findByPlanEstudios_Id(Long planEstudioId);
}
