package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.MateriaVistaExcel;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * Implementación del servicio que procesa un archivo Excel (.xlsx)
 * con el historial académico de un estudiante.
 *
 * <p>Convierte el contenido del archivo en una lista de objetos {@link MateriaVistaExcel},
 * representando cada materia cursada junto con su nota, semestre y tipo.
 * Valida la estructura del archivo y los encabezados requeridos antes de procesarlo.</p>
 *
 * <p>El archivo debe incluir las columnas:
 * <b>periodo, materia, créditos, semestre, nota, habilitación, definitiva, tipo</b>.</p>
 */
@Service
@RequiredArgsConstructor
public class ExcelHistorialAcademicoServiceImpl implements ExcelHistorialAcademicoService {
    /** Conjunto de encabezados obligatorios que deben estar presentes en el archivo Excel. */
    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "periodo", "materia", "créditos", "semestre", "nota", "habilitación", "definitiva", "tipo"
    );

    /**
     * Mapeo de alias de encabezados válidos hacia sus nombres canónicos.
     * <p>Permite aceptar variantes comunes (por ejemplo, "período" o "asignatura").</p>
     */
    private static final Map<String, String> HEADER_ALIASES = Map.ofEntries(
            Map.entry("periodo", "periodo"),
            Map.entry("período", "periodo"),
            Map.entry("materia", "materia"),
            Map.entry("asignatura", "materia"),
            Map.entry("créditos", "créditos"),
            Map.entry("creditos", "créditos"),
            Map.entry("semestre", "semestre"),
            Map.entry("nivel", "semestre"),
            Map.entry("nota", "nota"),
            Map.entry("habilitacion", "habilitación"),
            Map.entry("habilitación", "habilitación"),
            Map.entry("definitiva", "definitiva"),
            Map.entry("tipo", "tipo")
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MateriaVistaExcel> parsearHistorialAcademico(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo está vacío o no fue enviado.");
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("Archivo Excel sin hojas.");
            }

            // Leer encabezado
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("El archivo debe tener una fila de encabezado.");
            }

            Map<String, Integer> headerIndex = new HashMap<>();
            DataFormatter formatter = new DataFormatter();

            for (Cell cell : headerRow) {
                String raw = formatter.formatCellValue(cell);
                if (raw != null) {
                    String normalized = raw.trim().toLowerCase();
                    String canonical = HEADER_ALIASES.get(normalized);
                    if (canonical != null) {
                        headerIndex.put(canonical, cell.getColumnIndex());
                    }
                }
            }

            if (!headerIndex.keySet().containsAll(REQUIRED_HEADERS)) {
                throw new BusinessException("Formato inválido. El archivo debe contener columnas: " +
                        String.join(", ", REQUIRED_HEADERS));
            }

            List<MateriaVistaExcel> materias = new ArrayList<>();

            // Recorrer filas de datos
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String periodo = getCellString(row, headerIndex.get("periodo"), formatter);
                String nombre = getCellString(row, headerIndex.get("materia"), formatter);
                Integer creditos = getCellInteger(row, headerIndex.get("créditos"));
                Integer semestre = getCellInteger(row, headerIndex.get("semestre"));
                Double nota = getCellDouble(row, headerIndex.get("nota"));
                Double habilitacion = getCellDouble(row, headerIndex.get("habilitación"));
                Double definitiva = getCellDouble(row, headerIndex.get("definitiva"));
                String tipo = getCellString(row, headerIndex.get("tipo"), formatter);

                if ((nombre == null || nombre.isBlank()) && (creditos == null || semestre == null)) {
                    // Fila vacía, final del archivo
                    break;
                }

                MateriaVistaExcel materia = new MateriaVistaExcel();
                materia.setPeriodo(periodo);
                materia.setNombre(nombre);
                materia.setCreditos(creditos);
                materia.setSemestre(semestre);
                materia.setNota(nota);
                materia.setHabilitacion(habilitacion);
                materia.setDefinitiva(definitiva);
                materia.setTipo(tipo);

                materias.add(materia);
            }

            if (materias.isEmpty()) {
                throw new BusinessException("No se encontraron materias en el archivo.");
            }

            return materias;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Error procesando el archivo. Asegúrese de que sea un .xlsx con columnas válidas.");
        }
    }
    /**
     * Obtiene el valor de una celda como texto formateado.
     *
     * <p>Usa {@link DataFormatter} para mantener el formato original del Excel
     * (por ejemplo, valores con ceros a la izquierda o fechas).</p>
     *
     * @param row fila actual del archivo
     * @param colIndex índice de la columna a leer
     * @param formatter formateador de datos de Apache POI
     * @return valor de la celda como texto, o {@code null} si no existe
     */
    private String getCellString(Row row, Integer colIndex, DataFormatter formatter) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * Obtiene el valor de una celda numérica como entero.
     *
     * <p>Si la celda contiene texto numérico, intenta convertirlo a entero.
     * Si no es posible, devuelve {@code null}.</p>
     *
     * @param row fila actual del archivo
     * @param colIndex índice de la columna a leer
     * @return valor entero o {@code null} si no se puede convertir
     */
    private Integer getCellInteger(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else {
                String s = cell.toString().trim();
                return Integer.parseInt(s);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene el valor de una celda numérica como entero.
     *
     * <p>Si la celda contiene texto numérico, intenta convertirlo a entero.
     * Si no es posible, devuelve {@code null}.</p>
     *
     * @param row fila actual del archivo
     * @param colIndex índice de la columna a leer
     * @return valor entero o {@code null} si no se puede convertir
     */
    private Double getCellDouble(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else {
                String s = cell.toString().trim().replace(",", ".");
                return Double.parseDouble(s);
            }
        } catch (Exception e) {
            return null;
        }
    }

}
