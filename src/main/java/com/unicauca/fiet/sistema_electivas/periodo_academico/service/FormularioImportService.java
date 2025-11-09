package com.unicauca.fiet.sistema_electivas.periodo_academico.service;

import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoRespuestaFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestaOpcion;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.OfertaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestaOpcionRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import com.unicauca.fiet.sistema_electivas.programa.repository.ProgramaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio encargado de procesar e importar las respuestas de los formularios
 * cargados por los estudiantes en un período académico.
 *
 * <p>Este servicio toma los datos crudos leídos (por ejemplo, desde un archivo CSV o Excel),
 * los convierte en entidades del dominio {@link RespuestasFormulario} y sus
 * opciones asociadas ({@link RespuestaOpcion}), y los persiste en la base de datos.</p>
 *
 * <p>Durante el proceso:
 * <ul>
 *   <li>Asocia cada respuesta al {@link PeriodoAcademico} correspondiente.</li>
 *   <li>Identifica el {@link Programa} académico del estudiante.</li>
 *   <li>Relaciona cada opción elegida con su {@link Oferta} correspondiente.</li>
 * </ul>
 * </p>
 *
 * <p>Este servicio no realiza validaciones académicas ni reglas de negocio complejas;
 * su función principal es registrar correctamente la información recibida.
 * La validación posterior de las respuestas se realiza en el dominio
 * {@code procesamientovalidacion}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormularioImportService {

    private final ProgramaRepository programaRepository;
    private final RespuestasFormularioRepository respuestaRepository;
    private final RespuestaOpcionRepository opcionRepository;
    private final OfertaRepository ofertaRepository;
    /**
     * Procesa e inserta en la base de datos las respuestas de un formulario cargado.
     *
     * @param datosCrudos lista de mapas con los datos extraídos del archivo (una fila por estudiante)
     * @param periodo período académico al cual pertenecen las respuestas
     * @param archivo entidad que representa el archivo cargado originalmente
     * @return lista de entidades {@link RespuestasFormulario} creadas y persistidas
     */
    public List<RespuestasFormulario> procesarRespuestas(
            List<Map<String, String>> datosCrudos,
            PeriodoAcademico periodo,
            CargaArchivo archivo) {

        List<RespuestasFormulario> entidades = new ArrayList<>();

        for (Map<String, String> datos : datosCrudos) {
            RespuestasFormulario r = new RespuestasFormulario();
            r.setPeriodo(periodo);
            r.setArchivoCargado(archivo);
            r.setCodigoEstudiante(datos.get("Código del estudiante"));
            r.setCorreoEstudiante(datos.get("Correo institucional"));
            r.setNombreEstudiante(datos.get("Nombre"));
            r.setApellidosEstudiante(datos.get("Apellidos"));
            String ts = datos.get("timestampRespuesta");
            if (ts != null && !ts.isBlank()) {
                try {
                    // Declarar el formateador
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("yyyy/MM/dd h:mm:ss a 'GMT'X")
                            .toFormatter(Locale.forLanguageTag("es-CO"));
                    // Primero parseas a ZonedDateTime
                    ZonedDateTime zdt = ZonedDateTime.parse(ts.replace("p.m.", "PM").replace("a.m.", "AM"), formatter);
                    r.setTimestampRespuesta(zdt.toInstant());
                } catch (DateTimeParseException e) {
                    log.warn("No se pudo parsear la fecha '{}', se usa Instant.now(): {}", ts, e.getMessage());
                    r.setTimestampRespuesta(Instant.now());
                }
            } else {
                r.setTimestampRespuesta(Instant.now());
            }


            // Buscar programa
            String progTexto = datos.get("Programa académico");
            programaRepository.findByNombreIgnoreCase(progTexto).ifPresent(r::setPrograma);
            // Asignar estado inicial
            r.setEstado(EstadoRespuestaFormulario.SIN_PROCESAR);
            // Guardar cabecera
            respuestaRepository.save(r);

            // Crear opciones dinámicas
            short num = 1;
            for (String key : datos.keySet()) {
                if (key.startsWith("Electiva opción")) {
                    String electivaTexto = datos.get(key);

                    RespuestaOpcion op = new RespuestaOpcion();
                    op.setRespuesta(r);
                    op.setOpcionNum(num++);

                    // Buscar oferta por nombre de electiva y periodo
                    ofertaRepository.findByElectivaNombreIgnoreCaseAndPeriodo(electivaTexto, periodo)
                            .ifPresent(op::setOferta);

                    opcionRepository.save(op);
                }
            }

            entidades.add(r);
        }

        return entidades;
    }
    private static final Set<String> REQUIRED_HEADERS_RESPUESTAS = Set.of(
            "marca temporal",
            "correo institucional",
            "código del estudiante",
            "nombre",
            "apellidos",
            "programa académico",
            "electiva opción 1"
            // las demás opciones se validan dinámicamente
    );

    private static final Map<String, String> HEADER_ALIASES_RESPUESTAS = Map.ofEntries(
            Map.entry("marca temporal", "marca temporal"),
            Map.entry("timestamp", "marca temporal"),
            Map.entry("correo institucional", "correo institucional"),
            Map.entry("email", "correo institucional"),
            Map.entry("código del estudiante", "código del estudiante"),
            Map.entry("codigo del estudiante", "código del estudiante"),
            Map.entry("nombre", "nombre"),
            Map.entry("apellidos", "apellidos"),
            Map.entry("programa académico", "programa académico"),
            Map.entry("programa", "programa académico"),
            Map.entry("electiva opción 1", "electiva opción 1"),
            Map.entry("electiva opcion 1", "electiva opción 1")
    );
    /**
     * Parsea y valida un archivo de respuestas del formulario de preinscripción.
     *
     * <p>Lee el contenido del archivo (formato Excel .xlsx) y convierte cada fila en un mapa clave-valor
     * con los campos esperados, tales como {@code timestampRespuesta}, {@code correo_estudiante},
     * {@code codigo_estudiante}, {@code nombre_estudiante}, {@code apellidos_estudiante},
     * {@code programa_estudiante} y las columnas dinámicas de electivas.</p>
     *
     * <p>Acciones realizadas:
     * <ul>
     *   <li>Valida que el archivo no esté vacío y contenga encabezados válidos.</li>
     *   <li>Normaliza nombres de columnas y verifica la presencia de los encabezados requeridos.</li>
     *   <li>Transforma cada fila en un {@code Map<String, String>} según los encabezados encontrados.</li>
     *   <li>Ignora filas vacías (sin código ni correo).</li>
     *   <li>Lanza excepción si no se encuentran respuestas válidas.</li>
     * </ul>
     *
     * @param file Archivo Excel (.xlsx) que contiene las respuestas del formulario.
     * @param periodo Período académico asociado a las respuestas.
     * @return Lista de mapas con las respuestas parseadas y listas para su procesamiento.
     * @throws BusinessException Si el archivo es inválido, no contiene las columnas requeridas
     *         o no se pueden leer las filas correctamente.
     */
    public List<Map<String, String>> parsearRespuestasFormulario(MultipartFile file, PeriodoAcademico periodo) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo está vacío o no fue enviado.");
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("Archivo Excel sin hojas.");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("El archivo debe tener una fila de encabezado.");
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> headerIndex = new LinkedHashMap<>();

            for (Cell cell : headerRow) {
                String raw = formatter.formatCellValue(cell);
                if (raw == null) continue;

                String normalized = raw.trim().toLowerCase();
                String canonical = HEADER_ALIASES_RESPUESTAS.getOrDefault(normalized, normalized);
                headerIndex.put(canonical, cell.getColumnIndex());
            }

            // Validar que existan las mínimas columnas requeridas
            if (!headerIndex.keySet().containsAll(REQUIRED_HEADERS_RESPUESTAS)) {
                throw new BusinessException("Formato de archivo inválido. Asegúrese de que el Excel contenga las columnas: "
                        + String.join(", ", REQUIRED_HEADERS_RESPUESTAS) + ".");
            }

            List<Map<String, String>> respuestas = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                boolean filaVacia = true;
                for (Cell cell : row) {
                    if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                        filaVacia = false;
                        break;
                    }
                }
                if (filaVacia) continue;
                Map<String, String> datos = new LinkedHashMap<>();
                datos.put("timestampRespuesta", getCellString(row, headerIndex.get("marca temporal"), formatter));
                datos.put("Correo institucional", getCellString(row, headerIndex.get("correo institucional"), formatter));
                datos.put("Código del estudiante", getCellString(row, headerIndex.get("código del estudiante"), formatter));
                datos.put("Nombre", getCellString(row, headerIndex.get("nombre"), formatter));
                datos.put("Apellidos", getCellString(row, headerIndex.get("apellidos"), formatter));
                datos.put("Programa académico", getCellString(row, headerIndex.get("programa académico"), formatter));

                // Capturar opciones dinámicas según encabezado
                List<Map.Entry<String, Integer>> electivas = headerIndex.entrySet().stream()
                        .filter(e -> e.getKey().toLowerCase().startsWith("electiva opción"))
                        .sorted(Comparator.comparing(e -> extraerNumeroOpcion(e.getKey())))
                        .toList();

                short num = 1;
                for (Map.Entry<String, Integer> entry : electivas) {
                    String valor = getCellString(row, entry.getValue(), formatter);
                    datos.put("Electiva opción " + num++, valor);
                }
                // Filtrar filas vacías (sin código ni correo)
                if (datos.get("Código del estudiante") == null && datos.get("Correo institucional") == null) continue;

                respuestas.add(datos);
            }

            if (respuestas.isEmpty()) {
                throw new BusinessException("No se encontraron respuestas en el archivo.");
            }

            return respuestas;

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException("Error procesando el archivo. Asegúrese de que sea un .xlsx válido con las columnas requeridas.");
        }
    }
    private int extraerNumeroOpcion(String texto) {
        try {
            Matcher m = Pattern.compile("(\\d+)").matcher(texto);
            return m.find() ? Integer.parseInt(m.group(1)) : Integer.MAX_VALUE;
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
    /**
     * Obtiene el valor de una celda de Excel como texto formateado y limpio.
     *
     * <p>Este método evita errores cuando las celdas son nulas o están vacías,
     * y usa {@link DataFormatter} para convertir correctamente cualquier tipo
     * de celda (numérica, string, fecha, fórmula) en una representación textual.
     *
     * @param row       Fila actual de la hoja Excel.
     * @param colIndex  Índice de la columna a leer (puede ser nulo).
     * @param formatter Instancia de {@link DataFormatter} para formatear la celda.
     * @return Valor de la celda como texto limpio, o {@code null} si no existe.
     */
    private String getCellString(Row row, Integer colIndex, DataFormatter formatter) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return formatter.formatCellValue(cell).trim();
    }
}
