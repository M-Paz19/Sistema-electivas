package com.unicauca.fiet.sistema_electivas.reporte.service;

import com.unicauca.fiet.sistema_electivas.reporte.dto.ReporteArchivoResponse;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import org.apache.poi.ss.usermodel.Workbook;

public interface ReporteAsignacionService {

    /**
     * Genera el reporte técnico detallado para un período académico específico,
     * construyendo un archivo Excel que contiene:
     * <ul>
     *     <li>Una hoja principal con el ranking técnico de estudiantes.</li>
     *     <li>Hojas adicionales por cada departamento, incluyendo sus listas completas de asignación.</li>
     * </ul>
     *
     * <p>El método realiza las siguientes operaciones:</p>
     * <ol>
     *     <li>Valida que el período exista.</li>
     *     <li>Verifica que el período esté en estado
     *         {@link EstadoPeriodoAcademico#GENERACION_REPORTE_DETALLADO}; de lo contrario,
     *         lanza {@link InvalidStateException}.</li>
     *     <li>Construye un {@link Workbook} Excel con todas las hojas del reporte.</li>
     *     <li>Solicita al servicio de archivos guardar físicamente el reporte generado y registrar su
     *         metadato en la base de datos.</li>
     *     <li>Cambia el estado del período a
     *         {@link EstadoPeriodoAcademico#GENERACION_LISTAS_PUBLICAS} y lo persiste.</li>
     * </ol>
     *
     * <p>Este método encapsula toda la lógica de generación del archivo,
     * pero también retorna el {@link Workbook} en memoria para permitir que el controlador
     * lo entregue directamente como descarga al usuario si se desea.</p>
     *
     * @param periodoId identificador del período académico para el cual se generará el reporte.
     * @return el {@link Workbook} completamente generado, listo para ser descargado o manipulado.
     *
     * @throws ResourceNotFoundException si el período no existe.
     * @throws InvalidStateException si el período no está en estado GENERACION_REPORTE_DETALLADO.
     * @throws RuntimeException si ocurre algún error durante la construcción o almacenamiento del archivo.
     */
    Workbook generarReporteTecnico(Long periodoId);


    /**
     * Genera el reporte público (para publicación oficial).
     *
     * <p>Este reporte contiene:
     * <ul>
     *     <li>Solo información pública y autorizada</li>
     *     <li>Listado de códigos en orden</li>
     * </ul>
     *
     * <p>El archivo generado queda almacenado y listo para descarga.</p>
     *
     * @param periodoId ID del período académico
     * @return ruta del archivo generado
     */
    Workbook generarReportePublicacion(Long periodoId);

    /**
     * Obtiene el archivo del reporte técnico previamente generado
     * para el período indicado.
     */
    ReporteArchivoResponse obtenerArchivoReporteTecnico(Long periodoId);

    /**
     * Obtiene el archivo del reporte público previamente generado
     * para el período indicado.
     */
    ReporteArchivoResponse obtenerArchivoReportePublico(Long periodoId);
}
