package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.MateriaVistaExcel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelHistorialAcademicoService {
    /**
     <p>Lee el archivo Excel enviado, valida sus encabezados y convierte cada fila en una
     * instancia de {@link MateriaVistaExcel}. Si el formato no coincide con los encabezados requeridos
     * o el archivo está vacío, lanza una excepción de negocio.</p>
     *
     * @param file archivo Excel (.xlsx) que contiene el historial académico del estudiante
     * @return lista de materias encontradas en el archivo
     * @throws BusinessException si el archivo no cumple con el formato esperado o está vacío
     */
    List<MateriaVistaExcel> parsearHistorialAcademico(MultipartFile file);
}
