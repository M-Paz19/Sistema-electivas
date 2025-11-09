package com.unicauca.fiet.sistema_electivas.archivo.service;

import com.unicauca.fiet.sistema_electivas.archivo.dto.ArchivoResponse;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import org.springframework.core.io.Resource;

import java.util.List;

public interface ArchivoConsultaService {
    /**
     * Obtiene todos los archivos de tipo LOTES_CODIGOS asociados a un período académico.
     *
     * @param idPeriodo identificador del período académico
     * @return lista de archivos en formato {@link ArchivoResponse}
     */
    List<ArchivoResponse> obtenerLotesCodigosPorPeriodo(Long idPeriodo);
    /**
     * Descarga un archivo individual desde su ruta de almacenamiento en disco.
     *
     * @param idArchivo identificador del archivo
     * @return archivo como {@link Resource} listo para ser descargado
     * @throws ResourceNotFoundException si el archivo no existe o no puede accederse
     */
    Resource descargarArchivo(Long idArchivo);
    /**
     * Crea un archivo ZIP temporal con todos los archivos de tipo LOTES_CODIGOS
     * asociados a un período académico y lo devuelve como recurso descargable.
     *
     * @param idPeriodo identificador del período académico
     * @return archivo ZIP como {@link Resource}
     * @throws ResourceNotFoundException si no existen archivos asociados
     */
    Resource descargarLotesZip(Long idPeriodo);
}
