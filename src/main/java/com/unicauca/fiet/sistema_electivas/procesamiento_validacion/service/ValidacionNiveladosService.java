package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.ValidacionNiveladoResponseDTO;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.VerificacionNiveladoDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ValidacionNiveladosService {

    /**
     * Genera un reporte visual de la nivelación de los estudiantes.
     * 
     * <p>Lee el archivo Excel de historia académica, compara con la malla del programa
     * hasta el semestre actual del estudiante y genera una vista que muestra
     * el avance y posibles brechas.</p>
     *
     * @param idDatosAcademicos ID del estudiante que es posible nivelado
     * @param archivoExcel Archivo Excel con la historia académica de los estudiantes
     * @return DTO con la información lista para visualización en frontend
     */
    VerificacionNiveladoDTO generarReporteNivelado(MultipartFile archivoExcel, Long idDatosAcademicos);

    /**
     * Registra la decisión final del administrador sobre si un estudiante
     * cumple o no con el nivel de avance esperado.
     *
     * @param idDatosAcademicos ID del los datos academicos asociados al estudiante a tomar la decision
     * @param niveladoFinal true si el estudiante se considera nivelado, false en caso contrario
     * @return DTO con el resultado de la decisión
     */
    ValidacionNiveladoResponseDTO registrarDecisionFinal(Long idDatosAcademicos, boolean niveladoFinal);
}
