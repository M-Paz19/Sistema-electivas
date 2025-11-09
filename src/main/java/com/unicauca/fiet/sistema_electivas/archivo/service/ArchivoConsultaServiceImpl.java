package com.unicauca.fiet.sistema_electivas.archivo.service;

import com.unicauca.fiet.sistema_electivas.archivo.dto.ArchivoResponse;
import com.unicauca.fiet.sistema_electivas.archivo.enums.TipoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.mapper.ArchivoMapper;
import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.repository.CargaArchivoRepository;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.Resource;


/**
 * Servicio encargado de gestionar la recuperación de archivos almacenados
 * asociados a períodos académicos, incluyendo los lotes generados para SIMCA.
 */
@Service
@RequiredArgsConstructor
public class ArchivoConsultaServiceImpl implements ArchivoConsultaService {

    private final CargaArchivoRepository cargaArchivoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ArchivoResponse> obtenerLotesCodigosPorPeriodo(Long idPeriodo) {
        List<CargaArchivo> archivos = cargaArchivoRepository.findByPeriodoIdAndTipoArchivo(
                idPeriodo,
                TipoArchivo.LOTES_CODIGOS
        );

        return archivos.stream()
                .map(ArchivoMapper::toResponse)
                .collect(Collectors.toList());
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Resource descargarArchivo(Long idArchivo) {
        CargaArchivo archivo = cargaArchivoRepository.findById(idArchivo)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado."));

        Path filePath = Paths.get(archivo.getRutaAlmacenamiento());
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("El archivo no existe en el sistema de almacenamiento.");
        }

        try {

            return (Resource) new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al acceder al archivo: " + e.getMessage(), e);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Resource descargarLotesZip(Long idPeriodo) {
        List<CargaArchivo> archivos = cargaArchivoRepository.findByPeriodoIdAndTipoArchivo(
                idPeriodo, TipoArchivo.LOTES_CODIGOS
        );

        if (archivos.isEmpty()) {
            throw new ResourceNotFoundException("No hay archivos de lotes generados para este período.");
        }

        try {
            // Crear archivo ZIP temporal
            Path zipPath = Files.createTempFile("Lotes_SIMCA_" + idPeriodo + "_", ".zip");

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                for (CargaArchivo archivo : archivos) {
                    Path filePath = Paths.get(archivo.getRutaAlmacenamiento());
                    if (Files.exists(filePath)) {
                        zos.putNextEntry(new ZipEntry(filePath.getFileName().toString()));
                        Files.copy(filePath, zos);
                        zos.closeEntry();
                    }
                }
            }

            return new UrlResource(zipPath.toUri());

        } catch (IOException e) {
            throw new RuntimeException("Error al crear archivo ZIP: " + e.getMessage(), e);
        }
    }
}