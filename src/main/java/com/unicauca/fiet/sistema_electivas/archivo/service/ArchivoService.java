package com.unicauca.fiet.sistema_electivas.archivo.service;

import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * Sobrecarga de 'generarArchivosLotesSimca' que permite añadir un sufijo
     * al nombre del archivo (ej. "_CORREGIDOS").
     *
     * @param lotes Lista de lotes, donde cada sublista contiene los códigos de estudiantes.
     * @param periodo Periodo académico al que se asocian los lotes.
     * @param sufijoNombreArchivo Sufijo para agregar al nombre del archivo (ej. "_CORREGIDOS").
     * @return Lista de entidades CargaArchivo generadas.
     */
    List<CargaArchivo> generarArchivosLotesSimca(List<List<String>> lotes, PeriodoAcademico periodo, String sufijoNombreArchivo);

    /**
     * Carga un archivo previamente guardado como un recurso (Resource).
     *
     * @param nombreArchivo Nombre del archivo guardado (UUID + extensión).
     * @param tipo Tipo de archivo (usado para determinar la subcarpeta).
     * @return El archivo como un Resource de Spring.
     */
    Resource cargarArchivoComoRecurso(String nombreArchivo, String tipo);

    /**
     * Guarda un archivo subido (MultipartFile) en el sistema de archivos.
     * @param archivo Archivo subido por el usuario.
     * @param tipo Tipo de archivo (usado para determinar la subcarpeta).
     * @return El nombre único (UUID + extensión) con el que se guardó el archivo.
     */
    String guardarArchivo(MultipartFile archivo, String tipo);
    /**
     * Guarda un archivo Excel de datos académicos (SIMCA),
     * lo registra en la tabla carga_archivo y retorna la entidad guardada.
     *
     * @param archivo archivo Excel cargado por el usuario
     * @param periodo período académico asociado
     * @return la entidad CargaArchivo registrada
     */
    CargaArchivo guardarArchivoDatosAcademicos(MultipartFile archivo, PeriodoAcademico periodo);
}