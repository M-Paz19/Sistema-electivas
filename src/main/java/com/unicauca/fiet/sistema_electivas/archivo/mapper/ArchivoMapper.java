package com.unicauca.fiet.sistema_electivas.archivo.mapper;


import com.unicauca.fiet.sistema_electivas.archivo.dto.ArchivoResponse;
import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;

/**
 * Clase utilitaria encargada de transformar objetos entre la entidad {@link CargaArchivo}
 * y sus correspondientes DTOs.
 *
 * <p>Permite construir y transformar representaciones de archivos cargados desde y hacia
 * sus modelos utilizados en las capas de aplicación y presentación.</p>
 */
public class ArchivoMapper {

    /**
     * Convierte una entidad {@link CargaArchivo} en su representación {@link ArchivoResponse}.
     *
     * <p>Este método es utilizado para exponer la información esencial del archivo cargado,
     * evitando retornar datos internos o sensibles del modelo de persistencia.</p>
     *
     * @param archivo entidad {@link CargaArchivo} a convertir
     * @return DTO {@link ArchivoResponse} con los datos listos para enviar al cliente
     */
    public static ArchivoResponse toResponse(CargaArchivo archivo) {
        if (archivo == null) return null;

        return new ArchivoResponse(
                archivo.getId(),
                archivo.getNombreArchivo(),
                archivo.getEstado().getDescripcion(),
                archivo.getFechaCarga()
        );
    }
}
