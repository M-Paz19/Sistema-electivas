package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.repository;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DatosAcademicoRepository extends JpaRepository<DatosAcademico, Long> {

    /**
     * Busca todos los datos académicos asociados a un período académico específico,
     * navegando a través de la entidad CargaArchivo.
     *
     * Esta consulta se traduce a:
     * "FROM DatosAcademico d WHERE d.archivoCargado.periodo.id = :periodoId"
     *
     * @param periodoId ID del PeriodoAcademico.
     * @return Lista de DatosAcademico cargados para ese período.
     */
    List<DatosAcademico> findByArchivoCargado_Periodo_Id(Long periodoId);

}

