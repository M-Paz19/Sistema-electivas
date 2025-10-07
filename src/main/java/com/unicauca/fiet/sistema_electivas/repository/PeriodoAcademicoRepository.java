package com.unicauca.fiet.sistema_electivas.repository;

import com.unicauca.fiet.sistema_electivas.model.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Long> {
    Optional<PeriodoAcademico> findBySemestre(String semestre);
}
