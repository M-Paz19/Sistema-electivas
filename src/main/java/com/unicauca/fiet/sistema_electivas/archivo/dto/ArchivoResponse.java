package com.unicauca.fiet.sistema_electivas.archivo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
/**
 * DTO que representa los datos básicos de un archivo cargado en el sistema.
 *
 * <p>Contiene información útil para la capa de presentación o comunicación externa,
 * como el nombre del archivo, la ruta de almacenamiento y la fecha de carga.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoResponse {
    private Long idArchivo;
    private String nombreArchivo;
    private String EstadoArchivo;
    private Instant fechaCarga;
}