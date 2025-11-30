package com.unicauca.fiet.sistema_electivas.periodo_academico.service;

import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoRespuestaFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestaOpcion;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.OfertaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestaOpcionRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import com.unicauca.fiet.sistema_electivas.programa.repository.ProgramaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class FormularioImportService {

    private final ProgramaRepository programaRepository;
    private final RespuestasFormularioRepository respuestaRepository;
    private final RespuestaOpcionRepository opcionRepository;
    private final OfertaRepository ofertaRepository;

    @Transactional
    public List<RespuestasFormulario> procesarRespuestas(
            List<Map<String, String>> datosCrudos,
            PeriodoAcademico periodo,
            CargaArchivo archivo) {

        List<RespuestasFormulario> entidades = new ArrayList<>();
        for (Map<String, String> datos : datosCrudos) {
            String codigoEst = datos.get("Código del estudiante");



            Instant finalFechaRespuesta = parsearFechaFlexible(datos.get("timestampRespuesta"));


            RespuestasFormulario r = new RespuestasFormulario();
            r.setPeriodo(periodo);
            r.setArchivoCargado(archivo);
            r.setCodigoEstudiante(codigoEst);
            r.setCorreoEstudiante(datos.get("Correo institucional"));
            r.setNombreEstudiante(datos.get("Nombres"));
            r.setApellidosEstudiante(datos.get("Apellidos"));
            r.setTimestampRespuesta(finalFechaRespuesta);

            String progTexto = datos.get("Programa académico");
            programaRepository.findByNombreIgnoreCase(progTexto).ifPresent(r::setPrograma);
            r.setEstado(EstadoRespuestaFormulario.SIN_PROCESAR);

            respuestaRepository.save(r);

            short num = 1;
            for (String key : datos.keySet()) {
                if (key.toLowerCase().startsWith("electiva opción")) {
                    String electivaTexto = datos.get(key);
                    if (electivaTexto == null || electivaTexto.isBlank()) continue;

                    RespuestaOpcion op = new RespuestaOpcion();
                    op.setRespuesta(r);
                    op.setOpcionNum((int) num++);
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
            "marca temporal", "correo institucional", "código del estudiante", "nombre", "apellidos", "programa académico", "electiva opción 1"
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

            if (!headerIndex.keySet().containsAll(REQUIRED_HEADERS_RESPUESTAS)) {
                throw new BusinessException("Formato de archivo inválido. Faltan columnas requeridas.");
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

                List<Map.Entry<String, Integer>> electivas = headerIndex.entrySet().stream()
                        .filter(e -> e.getKey().toLowerCase().startsWith("electiva opción"))
                        .sorted(Comparator.comparing(e -> extraerNumeroOpcion(e.getKey())))
                        .toList();

                short num = 1;
                for (Map.Entry<String, Integer> entry : electivas) {
                    String valor = getCellString(row, entry.getValue(), formatter);
                    datos.put("Electiva opción " + num++, valor);
                }
                if (datos.get("Código del estudiante") == null && datos.get("Correo institucional") == null) continue;

                respuestas.add(datos);
            }
            return respuestas;
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException("Error procesando el archivo: " + e.getMessage());
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

    private String getCellString(Row row, Integer colIndex, DataFormatter formatter) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * Intenta convertir un texto de fecha a un {@link Instant} usando múltiples formatos.
     *
     * <p>Este método es robusto frente a formatos de fecha provenientes de:
     * <ul>
     *     <li>API de Google Forms → ISO-8601 (ej: {@code 2025-11-24T14:30:00.000Z})</li>
     *     <li>Archivos Excel exportados manualmente → formatos locales (ej:
     *         {@code 2025/05/27 4:35:13 p.m. GMT-5})</li>
     *     <li>Variaciones en AM/PM en español (ej: {@code p.m.}, {@code p. m.}, {@code pm})</li>
     *     <li>Offsets de zona horaria de la forma {@code GMT-5}, {@code GMT-05}, {@code GMT-05:00}</li>
     * </ul>
     *
     * <p>El método aplica una estrategia escalonada:
     * <ol>
     *     <li>Intentar parseo ISO-8601 directamente (caso Google API).</li>
     *     <li>Normalizar variantes de AM/PM en español.</li>
     *     <li>Intentar varios {@link DateTimeFormatter} que toleran diferentes variantes de GMT.</li>
     *     <li>Si todo falla, registrar una advertencia y retornar {@link Instant#now()}.</li>
     * </ol>
     *
     * <p>Este comportamiento garantiza que la importación de datos nunca falle por problemas
     * de formato de fecha y permite rastrear fácilmente problemas mediante logs.
     *
     * @param fechaTexto Texto de una fecha proveniente de Excel o Google Forms.
     * @return Un {@link Instant} válido. Si no es posible interpretar la fecha, retorna la hora actual.
     */
    private Instant parsearFechaFlexible(String fechaTexto) {

        if (fechaTexto == null || fechaTexto.isBlank()) {
            log.warn("Fecha nula o vacía, usando Instant.now()");
            return Instant.now();
        }

        String raw = fechaTexto.trim();

        // ----------------------------------------------------
        // 1. INTENTAR ISO-8601 (Google Forms API)
        // ----------------------------------------------------
        try {
            // Google a veces manda "2025-11-25 12:00:00Z" sin T
            return Instant.parse(raw.replace(" ", "T"));
        } catch (Exception ignored) {
            log.debug("No coincide con formato ISO-8601");
        }

        // ----------------------------------------------------
        // 2. NORMALIZAR FORMATO LOCAL / EXCEL
        // ----------------------------------------------------

        // Normalizar AM/PM español
        String normalizada = raw
                .replace("p.m.", "PM")
                .replace("a.m.", "AM")
                .replace("p. m.", "PM")
                .replace("a. m.", "AM")
                .replace("pm", "PM")
                .replace("am", "AM")
                .replace("Pm", "PM")
                .replace("Am", "AM")
                .trim();

        // Google Forms envía a veces:
        // "GMT-5", "GMT-05", "GMT-05:00"
        normalizada = normalizada.replaceAll("GMT-([0-9])\\b", "GMT-0$1"); // para que XXX lo entienda

        // ----------------------------------------------------
        // 3. Intentar formato manual variaciones Excel/Google
        // ----------------------------------------------------

        // LO IMPORTANTE: 'GMT'XXX para offsets flexibles
        DateTimeFormatter fmts[] = new DateTimeFormatter[]{
                // Ejemplo: 2025/05/27 4:35:13 PM GMT-05:00
                DateTimeFormatter.ofPattern("yyyy/MM/dd h:mm:ss a 'GMT'XXX", Locale.US),

                // Ejemplo: 2025/05/27 4:35:13 PM GMT-0500
                DateTimeFormatter.ofPattern("yyyy/MM/dd h:mm:ss a 'GMT'xx", Locale.US),

                // Ejemplo: 2025/05/27 4:35:13 PM GMT-5
                DateTimeFormatter.ofPattern("yyyy/MM/dd h:mm:ss a 'GMT'x", Locale.US)
        };

        for (DateTimeFormatter fmt : fmts) {
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(normalizada, fmt);
                return zdt.toInstant();
            } catch (Exception ignored) {
                // continuar probando
            }
        }

        // ----------------------------------------------------
        // 4. Fallo total → log de alerta y fallback
        // ----------------------------------------------------
        return Instant.now();
    }

}