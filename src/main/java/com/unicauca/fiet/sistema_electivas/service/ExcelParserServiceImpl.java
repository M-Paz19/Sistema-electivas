package com.unicauca.fiet.sistema_electivas.service;

import com.unicauca.fiet.sistema_electivas.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.model.PlanMateria;
import com.unicauca.fiet.sistema_electivas.service.ExcelParserService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * Implementación que usa Apache POI para convertir un .xlsx en PlanMateria.
 */
@Service
@RequiredArgsConstructor
public class ExcelParserServiceImpl implements ExcelParserService {

    private static final Set<String> REQUIRED_HEADERS_PLAN = Set.of("codigo", "nombre", "creditos", "semestre");
    private static final Map<String, String> HEADER_ALIASES = Map.ofEntries(
            Map.entry("codigo", "codigo"),
            Map.entry("código", "codigo"),
            Map.entry("codigo materia", "codigo"),
            Map.entry("código materia", "codigo"),

            Map.entry("nombre", "nombre"),
            Map.entry("materia", "nombre"),

            Map.entry("creditos", "creditos"),
            Map.entry("créditos", "creditos"),

            Map.entry("semestre", "semestre"),
            Map.entry("nivel", "semestre"),
            Map.entry("sem.", "semestre")
    );

    @Override
    public List<PlanMateria> parsearMaterias(MultipartFile file, PlanEstudio plan) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo está vacío o no fue enviado.");
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("Archivo Excel sin hojas.");
            }

            // Leer encabezado (primera fila)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("El archivo debe tener una fila de encabezado.");
            }

            // Mapear nombre de columna -> índice de columna (case-insensitive)
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


            if (!headerIndex.keySet().containsAll(REQUIRED_HEADERS_PLAN)) {
                throw new BusinessException("Formato inválido. El archivo debe contener encabezados equivalentes a: codigo, nombre, creditos, semestre.");
            }

            List<PlanMateria> materias = new ArrayList<>();

            // Recorrer filas a partir de la segunda (i = 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String codigo = getCellString(row, headerIndex.get("codigo"), formatter);
                String nombre = getCellString(row, headerIndex.get("nombre"), formatter);
                Integer creditos = getCellInteger(row, headerIndex.get("creditos"));
                Integer semestre = getCellInteger(row, headerIndex.get("semestre"));

                // Verificar si la fila está "vacía"
                int vacios = 0;
                if (codigo == null || codigo.isBlank()) vacios++;
                if (nombre == null || nombre.isBlank()) vacios++;
                if (creditos == null) vacios++;
                if (semestre == null) vacios++;

                if (vacios >= 3) {
                    // asumimos que ya no hay más datos → detener procesamiento
                    break;
                }

                // Validaciones normales (errores si falta un campo obligatorio)
                if (nombre == null || nombre.isBlank()) {
                    throw new BusinessException("Fila " + (i + 1) + ": el campo 'nombre' es obligatorio.");
                }
                if (creditos == null || creditos <= 0) {
                    throw new BusinessException("Fila " + (i + 1) + ": 'creditos' debe ser un entero positivo.");
                }
                if (semestre == null || semestre <= 0) {
                    throw new BusinessException("Fila " + (i + 1) + ": 'semestre' debe ser un entero positivo.");
                }

                PlanMateria materia = new PlanMateria();
                materia.setNombre(nombre.trim());
                materia.setSemestre(semestre);
                materia.setTipo(inferirTipo(nombre));
                materia.setCreditos(creditos);
                materia.setPlanEstudios(plan);

                materias.add(materia);
            }


            if (materias.isEmpty()) {
                throw new BusinessException("No se encontraron materias en el archivo.");
            }

            return materias;

        } catch (BusinessException be) {
            throw be; // re-lanzar para que el handler global lo convierta en BAD_REQUEST
        } catch (Exception e) {
            throw new BusinessException("Error procesando el archivo. Asegúrese de que sea un .xlsx con columnas: codigo,nombre,creditos,semestre.");
        }
    }

    private String inferirTipo(String nombreMateria) {
        if (nombreMateria == null) {
            return "OBLIGATORIA"; // fallback por defecto
        }

        String nombre = nombreMateria.toLowerCase();

        // Electiva (pero no Fish)
        if ((nombre.contains("electiva") || nombre.contains("elective"))
                && !nombre.contains("fish")) {
            return "ELECTIVA";
        }

        // Trabajo de grado
        if (nombre.contains("trabajo de grado")
                || nombre.contains("thesis")
                || nombre.contains("degree project")) {
            return "TRABAJO_GRADO";
        }

        // Por defecto
        return "OBLIGATORIA";
    }



    private String getCellString(Row row, Integer colIndex, DataFormatter formatter) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return formatter.formatCellValue(cell).trim();
    }

    private Integer getCellInteger(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            double d = cell.getNumericCellValue();
            return (int) d;
        } else {
            String s = cell.toString().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
