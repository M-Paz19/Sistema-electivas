package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException; // Importar BusinessException
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import org.apache.poi.ss.usermodel.*; // Importar Apache POI
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream; // Importar InputStream
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set; // Importar Set

@Service
public class SimcaCsvParserService {

    // Columnas requeridas según HU 2.1.1 (convertido a Set para validación)
    private static final Set<String> COLUMNAS_REQUERIDAS = Set.of(
            "CODIGO", "APELLIDOS", "NOMBRES", "USUARIO", "PROGRAMA",
            "CREDITOS_APROBADOS", "PERIODOS_MATRICULADOS", "PROMEDIO_CARRERA", "APROBADAS"
    );

    // Mapeo de alias para flexibilizar la lectura de encabezados
    private static final Map<String, String> HEADER_ALIASES = Map.ofEntries(
            Map.entry("codigo", "CODIGO"),
            Map.entry("apellidos", "APELLIDOS"),
            Map.entry("nombres", "NOMBRES"),
            Map.entry("usuario", "USUARIO"),
            Map.entry("programa", "PROGRAMA"),
            Map.entry("creditos_aprobados", "CREDITOS_APROBADOS"),
            Map.entry("créditos_aprobados", "CREDITOS_APROBADOS"), // Con tilde
            Map.entry("periodos_matriculados", "PERIODOS_MATRICULADOS"),
            Map.entry("períodos_matriculados", "PERIODOS_MATRICULADOS"), // Con tilde
            Map.entry("promedio_carrera", "PROMEDIO_CARRERA"),
            Map.entry("aprobadas", "APROBADAS")
    );

    public List<DatosAcademico> parsearArchivoSimca(MultipartFile archivo) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("El archivo está vacío o no fue enviado.");
        }

        List<DatosAcademico> listaDatos = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter(); // Usado para leer celdas como texto

        try (InputStream is = archivo.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("Archivo Excel sin hojas.");
            }

            // Leer encabezado (fila 0)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("El archivo debe tener una fila de encabezado.");
            }

            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String rawValue = dataFormatter.formatCellValue(cell);
                if (rawValue != null) {
                    String normalized = rawValue.trim().toLowerCase().replace(" ", "_");
                    String canonical = HEADER_ALIASES.get(normalized);
                    if (canonical != null) {
                        headerMap.put(canonical, cell.getColumnIndex());
                    }
                }
            }

            // Validación de estructura y campos (HU 2.1.1.2) [cite: 18]
            if (!headerMap.keySet().containsAll(COLUMNAS_REQUERIDAS)) {
                throw new BusinessException("Error en el archivo '" + archivo.getOriginalFilename() +
                        "'. Asegúrese de que el archivo tenga todas las columnas requeridas: " +
                        String.join(", ", COLUMNAS_REQUERIDAS) + " [cite: 18]");
            }

            // Recorrer filas de datos (desde la fila 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Obtener valores usando los helpers
                    String codigo = getCellString(row, headerMap.get("CODIGO"), dataFormatter);

                   // Ignorar la última línea de advertencia (si existe) [cite: 47]
                    if (codigo != null && codigo.startsWith("La División de Admisiones")) {
                        continue;
                    }

                    // Si el código está vacío, asumimos que es el final del archivo
                    if (codigo == null || codigo.isBlank()) {
                        break;
                    }

                    String apellidos = getCellString(row, headerMap.get("APELLIDOS"), dataFormatter);
                    String nombres = getCellString(row, headerMap.get("NOMBRES"), dataFormatter);
                    String usuario = getCellString(row, headerMap.get("USUARIO"), dataFormatter);
                    String programa = getCellString(row, headerMap.get("PROGRAMA"), dataFormatter);
                    Integer creditos = getCellInteger(row, headerMap.get("CREDITOS_APROBADOS"));
                    Integer periodos = getCellInteger(row, headerMap.get("PERIODOS_MATRICULADOS"));
                    BigDecimal promedio = getCellBigDecimal(row, headerMap.get("PROMEDIO_CARRERA"));
                    Integer aprobadas = getCellInteger(row, headerMap.get("APROBADAS"));

                    // Crear la entidad
                    DatosAcademico datos = new DatosAcademico();
                    datos.setCodigoEstudiante(codigo);
                    datos.setApellidos(apellidos);
                    datos.setNombres(nombres);
                    datos.setUsuario(usuario);
                    datos.setPrograma(programa);
                    datos.setCreditosAprobados(creditos);
                    datos.setPeriodosMatriculados(periodos);
                    datos.setPromedioCarrera(promedio);
                    datos.setAprobadas(aprobadas);

                    // Valores por defecto
                    datos.setEsNivelado(false);
                    datos.setPorcentajeAvance(BigDecimal.ZERO);
                    datos.setEstadoAptitud(EstadoAptitud.PENDIENTE_VALIDACION);

                    listaDatos.add(datos);

                } catch (Exception e) {
                    System.err.println("Error procesando fila de Excel: " + i + " | Error: " + e.getMessage());
                    // Continuar con la siguiente fila
                }
            }
        } catch (Exception e) {
            throw new BusinessException("Error procesando el archivo. Asegúrese de que sea un archivo Excel (.xls o .xlsx) válido.");
        }

        return listaDatos;
    }

    /**
     * Métodos de ayuda (inspirados en tu clase de ejemplo)
     */

    /**
     * Obtiene el valor de una celda como texto formateado.
     */
    private String getCellString(Row row, Integer colIndex, DataFormatter formatter) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * Obtiene el valor de una celda numérica como entero.
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
     * Obtiene el valor de una celda numérica como BigDecimal.
     */
    private BigDecimal getCellBigDecimal(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else {
                // Manejar comas decimales (ej. 3,685)
                String s = cell.toString().trim().replace(",", ".");
                return new BigDecimal(s);
            }
        } catch (Exception e) {
            return null;
        }
    }
}