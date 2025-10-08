package com.unicauca.fiet.sistema_electivas.plan_estudio.service;

import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanMateria;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Servicio encargado de parsear un archivo Excel (.xlsx) y producir entidades PlanMateria.
 */
public interface ExcelParserService {
    /**
     * Parsea el archivo Excel y retorna la lista de PlanMateria (no persistidas).
     * Valida el formato de columnas y los tipos de datos.
     *
     * Columnas esperadas (encabezado): "codigo", "nombre", "creditos", "semestre"
     *
     * @param file archivo .xlsx
     * @param plan plan al cual se asociarán las materias
     * @return lista de PlanMateria lista para persistir
     */
    List<PlanMateria> parsearMaterias(MultipartFile file, PlanEstudio plan);
}
