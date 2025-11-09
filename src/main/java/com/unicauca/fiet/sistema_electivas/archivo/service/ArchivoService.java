package com.unicauca.fiet.sistema_electivas.archivo.service;

import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ArchivoService {
    /**
     * Genera un archivo CSV con las respuestas del formulario y lo registra en la BD.
     */
    CargaArchivo guardarArchivoRespuestas(List<Map<String, String>> respuestas, PeriodoAcademico periodo);
    /**
     * Genera archivos de lotes de códigos para SIMCA (máximo 50 por lote)
     * y los registra en la base de datos.
     *
     * @param lotes Lista de lotes, donde cada sublista contiene los códigos de estudiantes.
     * @param periodo Periodo académico asociado.
     * @return Lista de entidades CargaArchivo generadas.
     */
    List<CargaArchivo> generarArchivosLotesSimca(List<List<String>> lotes, PeriodoAcademico periodo);
}
