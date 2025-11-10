package com.unicauca.fiet.sistema_electivas.archivo.service;

import com.unicauca.fiet.sistema_electivas.archivo.enums.EstadoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.enums.TipoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.repository.CargaArchivoRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ArchivoServiceImpl implements ArchivoService {

    private final CargaArchivoRepository cargaArchivoRepository;

    // Variables de Path para gestionar las rutas (reemplazan a las constantes estáticas)
    private final Path storagePath;
    private final Path mallasPath;
    private final Path lotesSimcaPath;
    private final Path respuestasPath;
    private final Path datos_academicos;

    @Autowired
    public ArchivoServiceImpl(
            CargaArchivoRepository cargaArchivoRepository,
            @Value("${storage.path}") String storagePath,
            @Value("${storage.mallas-path}") String mallasPath,
            @Value("${storage.lotes-simca-path}") String lotesSimcaPath,
            @Value("${storage.respuestas-path}") String respuestasPath,
            @Value("${storage.datos_academicos}") String datos_academicos

    ) {
        this.cargaArchivoRepository = cargaArchivoRepository;

        // Inicializar rutas principales
        this.storagePath = Paths.get(storagePath).toAbsolutePath().normalize();
        this.mallasPath = this.storagePath.resolve(mallasPath).normalize();
        this.lotesSimcaPath = this.storagePath.resolve(lotesSimcaPath).normalize();
        this.respuestasPath = this.storagePath.resolve(respuestasPath).normalize();
        this.datos_academicos = this.storagePath.resolve(datos_academicos).normalize();

        try {
            // Crear todos los directorios necesarios si no existen
            Files.createDirectories(this.storagePath);
            Files.createDirectories(this.mallasPath);
            Files.createDirectories(this.lotesSimcaPath);
            Files.createDirectories(this.respuestasPath);
            Files.createDirectories(this.datos_academicos);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear uno o más directorios de almacenamiento.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CargaArchivo guardarArchivoRespuestas(List<Map<String, String>> respuestas, PeriodoAcademico periodo) {
        try {
            // Usa la variable Path inicializada en el constructor
            String fileName = "respuestas_" + periodo.getSemestre() + "_" + LocalDate.now() + ".csv";
            Path filePath = this.respuestasPath.resolve(fileName);

            int numeroOpciones = periodo.getNumeroOpcionesFormulario() != null
                    ? periodo.getNumeroOpcionesFormulario()
                    : 7;

            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write('\uFEFF'); // BOM

                // Cabecera
                List<String> cabecera = new ArrayList<>(List.of(
                        "Hora de envío", "Correo", "Código", "Nombre", "Apellidos", "Programa"
                ));
                for (int i = 1; i <= numeroOpciones; i++) {
                    cabecera.add("Electiva opción " + i);
                }
                writer.write(String.join(";", cabecera));
                writer.newLine();

                // Filas
                for (Map<String, String> fila : respuestas) {
                    List<String> valores = new ArrayList<>(List.of(
                            quote(fila.getOrDefault("timestampRespuesta", "")),
                            quote(fila.getOrDefault("Correo institucional", "")),
                            quote(fila.getOrDefault("Código del estudiante", "")),
                            quote(fila.getOrDefault("Nombre", "")),
                            quote(fila.getOrDefault("Apellidos", "")),
                            quote(fila.getOrDefault("Programa académico", ""))
                    ));
                    for (int i = 1; i <= numeroOpciones; i++) {
                        valores.add(quote(fila.getOrDefault("Electiva opción " + i, "")));
                    }
                    writer.write(String.join(";", valores));
                    writer.newLine();
                }
            }

            // Registrar el archivo en la BD
            CargaArchivo archivo = new CargaArchivo();
            archivo.setPeriodo(periodo);
            archivo.setTipoArchivo(TipoArchivo.RESPUESTAS_FORMULARIO);
            archivo.setNombreArchivo(fileName);
            archivo.setRutaAlmacenamiento(filePath.toString());
            archivo.setFechaCarga(Instant.now());
            archivo.setEstado(EstadoArchivo.CARGADO);

            cargaArchivoRepository.save(archivo);
            log.info("Archivo [{}] generado y registrado correctamente en {}", fileName, filePath);

            return archivo;

        } catch (IOException e) {
            log.error("Error al generar archivo CSV: {}", e.getMessage());
            throw new RuntimeException("Error al guardar archivo de respuestas", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<CargaArchivo> generarArchivosLotesSimca(List<List<String>> lotes, PeriodoAcademico periodo) {
        // Llama al nuevo método con un sufijo vacío
        return this.generarArchivosLotesSimca(lotes, periodo, "");
    }

    /**
     * Implementación del método sobrecargado con sufijo.
     */

    @Override
    @Transactional
    public List<CargaArchivo> generarArchivosLotesSimca(List<List<String>> lotes, PeriodoAcademico periodo, String sufijoNombreArchivo) {
        try {
            List<CargaArchivo> archivosGenerados = new ArrayList<>();

            for (int i = 0; i < lotes.size(); i++) {
                List<String> lote = lotes.get(i);

                // Generar nombre de archivo con el sufijo
                String fileName = String.format("Lote_%d%s_Periodo_%s_CodigosSIMCA.txt",
                        i + 1, sufijoNombreArchivo, periodo.getSemestre());

                // Usa la variable Path inicializada en el constructor
                Path filePath = this.lotesSimcaPath.resolve(fileName);

                // Contenido separado por comas (según formato de SIMCA)
                String contenido = String.join(",", lote);

                // Escribir archivo
                try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                    writer.write(contenido);
                }

                // Registrar en BD
                CargaArchivo archivo = new CargaArchivo();
                archivo.setPeriodo(periodo);
                archivo.setTipoArchivo(TipoArchivo.LOTES_CODIGOS);
                archivo.setNombreArchivo(fileName);
                archivo.setRutaAlmacenamiento(filePath.toString());
                archivo.setFechaCarga(Instant.now());
                archivo.setEstado(EstadoArchivo.PROCESADO);

                cargaArchivoRepository.save(archivo);
                archivosGenerados.add(archivo);

                log.info("Lote {}{} generado correctamente: {}", i + 1, sufijoNombreArchivo, filePath);
            }

            return archivosGenerados;

        } catch (IOException e) {
            log.error("Error al generar archivos de lotes SIMCA: {}", e.getMessage());
            throw new RuntimeException("Error al generar archivos de lotes SIMCA", e);
        }
    }

    @Override
    public Resource cargarArchivoComoRecurso(String nombreArchivo, String tipo) {
        try {
            Path path = obtenerPath(tipo);
            Path filePath = path.resolve(nombreArchivo).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Archivo no encontrado " + nombreArchivo);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Archivo no encontrado " + nombreArchivo, ex);
        }
    }


    @Override
    public String guardarArchivo(MultipartFile file, String tipo) {
        // Limpiar el nombre del archivo
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Generar un nombre único
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String nombreArchivo = UUID.randomUUID().toString() + "." + extension;

        try {
            if (originalFilename.contains("..")) {
                throw new BusinessException("El nombre del archivo contiene una secuencia de ruta inválida " + originalFilename);
            }

            Path path = obtenerPath(tipo); // Obtiene la ruta base (ej. mallasPath)
            Path targetLocation = path.resolve(nombreArchivo);

            // Copiar el archivo
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return nombreArchivo;
        } catch (IOException ex) {
            throw new BusinessException("No se pudo guardar el archivo " + nombreArchivo + ". Inténtalo de nuevo.", ex);
        }
    }

    /**
     * Helper para obtener la ruta de almacenamiento
     * basada en el tipo de archivo.
     */
    private Path obtenerPath(String tipo) {

        if (tipo.equals(TipoArchivo.LOTES_CODIGOS.name())) {
            return this.lotesSimcaPath;
        }
        if (tipo.equals(TipoArchivo.RESPUESTAS_FORMULARIO.name())) {
            return this.respuestasPath;
        }
        // --- AÑADIR ESTA VALIDACIÓN ---
        if (tipo.equals(TipoArchivo.DATOS_ACADEMICOS.name())) {
            return this.datos_academicos;
        }
        // Por defecto, usar el path general
        return this.storagePath;
    }

    /**
     * Helper para escapar comillas en CSV.
     */
    private String quote(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

}