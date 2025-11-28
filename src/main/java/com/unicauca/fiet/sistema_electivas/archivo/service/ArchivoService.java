package com.unicauca.fiet.sistema_electivas.archivo.service;

import com.unicauca.fiet.sistema_electivas.archivo.enums.EstadoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.enums.TipoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import org.apache.poi.ss.usermodel.Workbook;
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

    /**
     * Genera y almacena en el sistema de archivos un reporte técnico detallado en formato Excel,
     * asociado a un período académico específico. Además, registra la información del archivo
     * generado en la base de datos dentro de la entidad {@link CargaArchivo}, marcándolo como
     * procesado.
     *
     * <p>El método realiza las siguientes operaciones:</p>
     * <ul>
     *     <li>Construye el nombre del archivo usando el semestre del período y la fecha actual.</li>
     *     <li>Escribe físicamente el {@link Workbook} recibido en disco dentro del directorio de reportes.</li>
     *     <li>Registra los metadatos del archivo en la base de datos, incluyendo:
     *         <ul>
     *             <li>Nombre del archivo</li>
     *             <li>Ruta de almacenamiento</li>
     *             <li>Fecha de carga</li>
     *             <li>Tipo de archivo ({@link TipoArchivo#REPORTE_DETALLADO})</li>
     *             <li>Estado del archivo ({@link EstadoArchivo#PROCESADO})</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param workbook el libro Excel previamente generado con el contenido del reporte técnico.
     * @param periodo el período académico al que pertenece el reporte.
     *
     * @return la entidad {@link CargaArchivo} persistida que representa el archivo almacenado.
     *
     * @throws RuntimeException si ocurre algún error al escribir el archivo en disco
     *                          o al almacenar el registro en la base de datos.
     */
    CargaArchivo guardarReporteDetallado(Workbook workbook, PeriodoAcademico periodo);

    /**
     * Guarda en el sistema de archivos el reporte público de asignaciones
     * (formato Excel) generado para un período académico y registra su
     * información en la base de datos.
     *
     * <p>El reporte almacenado corresponde a la versión pública utilizada
     * para publicación oficial, la cual no incluye datos sensibles
     * (como porcentajes de avance o indicadores de nivelación).</p>
     *
     * <p>El archivo se guarda en la ruta configurada para reportes y
     * se crea un registro {@link CargaArchivo} con su metadatos
     * (nombre, ruta, fecha y tipo de archivo).</p>
     *
     * @param workbook Workbook ya construido con el contenido del reporte público.
     * @param periodo  Entidad {@link PeriodoAcademico} al cual pertenece el reporte.
     * @return El registro {@link CargaArchivo} persistido en la base de datos.
     * @throws RuntimeException si ocurre algún error al escribir el archivo o
     *         al registrar la información en base de datos.
     */
    CargaArchivo guardarReportePublicacion(Workbook workbook, PeriodoAcademico periodo);
}