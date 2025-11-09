package com.unicauca.fiet.sistema_electivas.archivo.service;


import com.unicauca.fiet.sistema_electivas.archivo.enums.EstadoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.enums.TipoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.repository.CargaArchivoRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestaOpcion;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivoServiceImpl implements ArchivoService {

    private final CargaArchivoRepository cargaArchivoRepository;

    private static final String PATH_RESPUESTAS = "storage/respuestas_formulario/";
    private static final String PATH_LOTES_SIMCA = "storage/lotes_simca/";
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CargaArchivo guardarArchivoRespuestas(List<Map<String, String>> respuestas, PeriodoAcademico periodo) {
        try {
            // Crear directorio si no existe
            Path baseDir = ensureDirectoryExists(PATH_RESPUESTAS);

            String fileName = "respuestas_" + periodo.getSemestre() + "_" + LocalDate.now() + ".csv";
            Path filePath = baseDir.resolve(fileName);

            // Obtener número de opciones dinámico del periodo
            int numeroOpciones = periodo.getNumeroOpcionesFormulario() != null
                    ? periodo.getNumeroOpcionesFormulario()
                    : 7; // valor por defecto, por compatibilidad
            // Abrir escritor CSV
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                // Escribir BOM
                writer.write('\uFEFF');

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
        try {
            // Crear directorio si no existe
            Path baseDir = ensureDirectoryExists(PATH_LOTES_SIMCA);
            List<CargaArchivo> archivosGenerados = new ArrayList<>();

            for (int i = 0; i < lotes.size(); i++) {
                List<String> lote = lotes.get(i);

                // Generar nombre de archivo
                String fileName = String.format("Lote_%d_Periodo_%s_CodigosSIMCA.txt",
                        i + 1, periodo.getSemestre());
                Path filePath = baseDir.resolve(fileName);

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

                log.info("Lote {} generado correctamente: {}", i + 1, filePath);
            }

            return archivosGenerados;

        } catch (IOException e) {
            log.error("Error al generar archivos de lotes SIMCA: {}", e.getMessage());
            throw new RuntimeException("Error al generar archivos de lotes SIMCA", e);
        }
    }
    private Path ensureDirectoryExists(String dirPath) throws IOException {
        Path baseDir = Paths.get(dirPath);
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
        return baseDir;
    }
    private String quote(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
