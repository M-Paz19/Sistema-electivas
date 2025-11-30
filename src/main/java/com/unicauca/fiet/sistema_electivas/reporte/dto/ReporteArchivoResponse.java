package com.unicauca.fiet.sistema_electivas.reporte.dto;

import lombok.Data;
import org.springframework.core.io.Resource;

/**
 * DTO que encapsula el archivo generado para un reporte
 * junto con el nombre con el que fue almacenado.
 *
 * <p>Se utiliza para exponer el archivo como recurso descargable
 * y permitir que el cliente conozca su nombre real antes de la descarga.</p>
 */
@Data
public class ReporteArchivoResponse {

    /**
     * Recurso que representa el archivo físico almacenado.
     */
    private Resource archivo;

    /**
     * Nombre original con el que el archivo fue guardado
     * en el sistema de almacenamiento.
     */
    private String nombreArchivo;
}

