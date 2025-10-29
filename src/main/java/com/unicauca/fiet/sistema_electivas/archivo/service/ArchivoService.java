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
}
