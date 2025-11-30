package com.unicauca.fiet.sistema_electivas.reporte.service;

import com.unicauca.fiet.sistema_electivas.archivo.enums.TipoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.repository.CargaArchivoRepository;
import com.unicauca.fiet.sistema_electivas.archivo.service.ArchivoService;
import com.unicauca.fiet.sistema_electivas.asignacion.dto.*;
import com.unicauca.fiet.sistema_electivas.asignacion.service.ConsultaAsignacionService;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.reporte.dto.ReporteArchivoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteAsignacionServiceImpl implements ReporteAsignacionService {
    @Autowired
    private ConsultaAsignacionService consultaAsignacionService;
    @Autowired
    private PeriodoAcademicoRepository periodoAcademicoRepository;
    @Autowired
    private ArchivoService  archivoService;
    @Autowired
    private CargaArchivoRepository cargaArchivoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Workbook  generarReporteTecnico(Long periodoId) {

        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        // 2. Validar estado PROCESO_FILTRADO_NO_ELEGIBLES
        if (periodo.getEstado() != EstadoPeriodoAcademico.GENERACION_REPORTE_DETALLADO) {
            throw new InvalidStateException(
                    "Solo se puede generar el reporte detallado cuando el período está en estado GENERACION_REPORTE_DETALLADO."
            );
        }
        log.info("Generando reporte técnico (Ranking) para período {}", periodo.getSemestre());

        List<EstudianteAsignacionReporteResponse> ranking =
                consultaAsignacionService.generarReporteRanking(periodoId);

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Ranking Técnico");

        // Obtener mapa de opciones por programa
        Map<Long, Integer> opcionesPorPrograma = periodo.getOpcionesPorPrograma();

        // Calcular el máximo de opciones (defecto 1 si es nulo o vacío)
        int maxOpciones = 1;
        if (opcionesPorPrograma != null && !opcionesPorPrograma.isEmpty()) {
            maxOpciones = opcionesPorPrograma.values().stream()
                    .filter(Objects::nonNull)           // ignorar nulls por si acaso
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(1);                         // fallback si todos eran nulls
        }

        // Llamada al método con el máximo encontrado
        crearPrimeraHojaRanking(workbook, sheet, ranking, maxOpciones);


        List<DepartamentoReporteDTO> departamentos =
                consultaAsignacionService.generarListasDeAsigancionPorDepartamentos(periodoId);

        for (DepartamentoReporteDTO dep : departamentos) {

            String nombreHoja = "Listas " + dep.getNombre();
            if (nombreHoja.length() > 31) {
                nombreHoja = nombreHoja.substring(0, 31);
            }

            Sheet sheetDep = workbook.createSheet(nombreHoja);

            crearHojaDepartamento(sheetDep, dep, workbook);
        }
        archivoService.guardarReporteDetallado(workbook, periodo);
        periodo.setEstado(EstadoPeriodoAcademico.GENERACION_LISTAS_PUBLICAS);
        periodoAcademicoRepository.save(periodo);
        return workbook;
    }
    /**
     * Genera la hoja principal del reporte de ranking de estudiantes.
     *
     * <p>Construye el header, define los estilos (normales, por programa y por estado)
     * y escribe una fila por cada estudiante con su información académica y
     * sus asignaciones de electivas según el número de opciones configuradas.
     * Además, colorea dinámicamente las celdas según el programa del estudiante
     * o el estado de la asignación.</p>
     *
     * @param workbook       Libro Excel donde se creará la hoja.
     * @param sheet          Hoja a la cual se agregará la información.
     * @param ranking        Lista de estudiantes en el ranking.
     * @param numeroOpciones Número de columnas dinámicas de electivas por estudiante.
     */
    private void crearPrimeraHojaRanking(
            Workbook workbook,
            Sheet sheet,
            List<EstudianteAsignacionReporteResponse> ranking,
            Integer numeroOpciones
    ) {
        int rowIndex = 0;

        // ===== Estilo normal =====
        CellStyle styleNormal = workbook.createCellStyle();
        ActivaBordesCeldas(styleNormal);
        Font normalFont = workbook.createFont();
        normalFont.setBold(false);
        styleNormal.setFont(normalFont);

        // ===== Estilos por estado =====
        CellStyle styleAsignada = crearColorConBorde(workbook, IndexedColors.LIGHT_GREEN);
        CellStyle styleEspera = crearColorConBorde(workbook, IndexedColors.GREY_25_PERCENT);
        CellStyle styleSinCupo = crearColorConBorde(workbook, IndexedColors.ROSE);
        CellStyle styleProgIncompatible = crearColorConBorde(workbook, IndexedColors.LIGHT_YELLOW);
        CellStyle styleDuplicada = crearColorConBorde(workbook, IndexedColors.LAVENDER);
        CellStyle styleNivelado = crearColorConBorde(workbook, IndexedColors.AQUA);

        DataFormat df = workbook.createDataFormat();

        // Estilo normal con 4 decimales
        CellStyle styleNormal4 = workbook.createCellStyle();
        styleNormal4.cloneStyleFrom(styleNormal);
        styleNormal4.setDataFormat(df.getFormat("0.0000"));

        // Estilo nivelado con 4 decimales
        CellStyle styleNivelado4 = workbook.createCellStyle();
        styleNivelado4.cloneStyleFrom(styleNivelado);
        styleNivelado4.setDataFormat(df.getFormat("0.0000"));

        // ===== Estilo header =====
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // ===== Estilos por programa =====
        Map<String, CellStyle> estilosPorPrograma =
                crearEstilosPrograma(workbook, ranking, normalFont);
        // ===== HEADER =====
        Row header = sheet.createRow(rowIndex++);
        int col = 0;

        // Celdas fijas
        crearHeaderCell(header, col++, "POSICIÓN", headerStyle);
        crearHeaderCell(header, col++, "CODIGO", headerStyle);
        crearHeaderCell(header, col++, "APELLIDOS", headerStyle);
        crearHeaderCell(header, col++, "NOMBRES", headerStyle);
        crearHeaderCell(header, col++, "USUARIO", headerStyle);
        crearHeaderCell(header, col++, "PROGRAMA", headerStyle);
        crearHeaderCell(header, col++, "CR. APROB. TOTAL", headerStyle);
        crearHeaderCell(header, col++, "CR. APROB. (OBLIG)", headerStyle);
        crearHeaderCell(header, col++, "CR. PENSUM. (OBLIG)", headerStyle);
        crearHeaderCell(header, col++, "PERIODOS MATRICULADOS", headerStyle);
        crearHeaderCell(header, col++, "¿NIVELADO?", headerStyle);
        crearHeaderCell(header, col++, "PORCENTAJE AVANCE", headerStyle);
        crearHeaderCell(header, col++, "PROMEDIO CARRERA", headerStyle);
        crearHeaderCell(header, col++, "DEBE VER", headerStyle);
        crearHeaderCell(header, col++, "APROBADAS", headerStyle);
        crearHeaderCell(header, col++, "FALTAN", headerStyle);
        crearHeaderCell(header, col++, "ASIGNADAS", headerStyle);
        crearHeaderCell(header, col++, "LISTA DE ESPERA", headerStyle);

        // Columnas dinámicas
        int optionStartCol = col;
        for (int i = 1; i <= numeroOpciones; i++) {
            crearHeaderCell(header, col++, "Opción " + i, headerStyle);
        }

        // ===== CONTENIDO =====
        int pos = 1;

        // ========= FILAS ==========
        for (var est : ranking) {

            Row row = sheet.createRow(rowIndex++);
            int c = 0;

            CellStyle estiloProg = estilosPorPrograma.get(est.getPrograma());
            boolean nivelado = Boolean.TRUE.equals(est.getEsNivelado());

            crearCell(row, c++, pos++, styleNormal);
            crearCell(row, c++, est.getCodigoEstudiante(), styleNormal);
            crearCell(row, c++, est.getApellidos(), styleNormal);
            crearCell(row, c++, est.getNombres(), styleNormal);
            crearCell(row, c++, est.getUsuario(), styleNormal);
            crearCell(row, c++, est.getPrograma(), estiloProg);
            crearCell(row, c++, est.getCreditosAprobadosTotal(), estiloPorNivelado(nivelado, styleNivelado, styleNormal));
            crearCell(row, c++, est.getCreditosAprobadosObligatorio(), styleNormal);
            crearCell(row, c++, est.getCreditosPensumObligatorio(), estiloProg);
            crearCell(row, c++, est.getPeriodosMatriculados(), styleNormal);
            crearCell(row, c++, nivelado ? "Sí" : "No", estiloPorNivelado(nivelado, styleNivelado, styleNormal));
            crearCell(row, c++, est.getPorcentajeAvance() != null ? est.getPorcentajeAvance().doubleValue() : 0,
                    estiloPorNivelado(nivelado, styleNivelado4, styleNormal4));
            crearCell(row, c++, est.getPromedioCarrera() != null ? est.getPromedioCarrera().doubleValue() : 0,
                    styleNormal);
            crearCell(row, c++, est.getDebeVer(), estiloProg);
            crearCell(row, c++, est.getAprobadas(), styleNormal);
            crearCell(row, c++, est.getFaltan(), styleNormal);
            crearCell(row, c++, est.getAsignadas(), est.getAsignadas() > 0 ? styleAsignada : styleNormal);
            crearCell(row, c++, est.getListaDeEspera(), est.getListaDeEspera() > 0 ? styleEspera : styleNormal);

            List<EstudianteAsignacionReporteResponse.AsignacionElectivaInfo> asignaciones = est.getAsignaciones();

            for (int i = 1; i <= numeroOpciones; i++) {
                EstudianteAsignacionReporteResponse.AsignacionElectivaInfo asign =
                        buscarAsignacion(asignaciones, i);

                Cell cell = row.createCell(c++);

                if (asign == null) {
                    cell.setCellValue("");
                    cell.setCellStyle(styleNormal);
                    continue;
                }

                cell.setCellValue(asign.getNombreElectiva());

                if (asign.getEstado() != null) {
                    switch (asign.getEstado()) {
                        case ASIGNADA -> cell.setCellStyle(styleAsignada);
                        case LISTA_ESPERA -> cell.setCellStyle(styleEspera);
                        case SIN_CUPO, SIN_CUPO_LISTA_ESPERA -> cell.setCellStyle(styleSinCupo);
                        case PROGRAMA_INCOMPATIBLE -> cell.setCellStyle(styleProgIncompatible);
                        case NO_EVALUADA -> cell.setCellStyle(styleNormal);
                        case OPCION_DUPLICADA -> cell.setCellStyle(styleDuplicada);
                    }
                }
            }
        }

        for (int i = 0; i <= optionStartCol + numeroOpciones; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Genera dentro de la hoja indicada la estructura completa de reporte para un
     * departamento, creando una sección por cada oferta del departamento.
     *
     * <p>Cada oferta se representa como una tabla compuesta por:</p>
     * <ul>
     *     <li>Un título con el nombre de la electiva, abarcando 6 columnas.</li>
     *     <li>Una tabla fija de 25 filas correspondientes a las posiciones
     *         oficiales de los estudiantes (1 a 25), sin importar cuántos estudiantes
     *         tenga realmente la oferta.</li>
     *     <li>Las filas del 1 al 18 se formatean con estilo normal (ASIGNADOS) y las
     *         posiciones 19 a 25 con estilo de espera.</li>
     *     <li>Si no existe un estudiante para una posición, la fila se genera vacía,
     *         conservando la numeración.</li>
     *     <li>La columna de porcentaje de avance usa un estilo especial si el
     *         estudiante pertenece a un programa nivelado.</li>
     * </ul>
     *
     * <p>Todos los estilos (bordes, color, negrita) son configurados dentro del
     * método y aplicados según la naturaleza de cada celda.</p>
     *
     * @param sheet     hoja donde se escribirá la información del departamento
     * @param dep       DTO con datos del departamento y sus ofertas
     * @param workbook  libro Excel utilizado para la creación de estilos
     */
    private void crearHojaDepartamento(Sheet sheet, DepartamentoReporteDTO dep, Workbook workbook) {

        // === Definir estilos ===
        // ===== Estilo normal =====
        CellStyle styleNormal = workbook.createCellStyle();
        ActivaBordesCeldas(styleNormal);
        Font normalFont = workbook.createFont();
        normalFont.setBold(false);
        styleNormal.setFont(normalFont);
        CellStyle styleTitulo = crearColorConBorde(workbook, IndexedColors.GREY_25_PERCENT);

        // Crear fuente en negrita
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        styleTitulo.setFont(boldFont);

        // Centrar el texto
        styleTitulo.setAlignment(HorizontalAlignment.CENTER);
        styleTitulo.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle styleEspera = crearColorConBorde(workbook, IndexedColors.BLUE_GREY);
        CellStyle styleNivelado = crearColorConBorde(workbook, IndexedColors.AQUA);

        DataFormat df = workbook.createDataFormat();
        // Crear estilo nivelado con 4 decimales
        CellStyle styleNivelado4Decimales = workbook.createCellStyle();
        styleNivelado4Decimales.cloneStyleFrom(styleNivelado);
        styleNivelado4Decimales.setDataFormat(df.getFormat("0.0000"));


        int rowIndex = 0;

        for (OfertaReporteDTO oferta : dep.getOfertas()) {

            // === Título de la oferta ===
            Row rowTitulo = sheet.createRow(rowIndex++);
            Cell tituloCell = rowTitulo.createCell(0);
            tituloCell.setCellValue(oferta.getNombreElectiva());
            tituloCell.setCellStyle(styleTitulo);

            // Merge A1:F1 -> 6 columnas
            sheet.addMergedRegion(new CellRangeAddress(
                    rowIndex - 1, rowIndex - 1, 0, 5
            ));


            // === Filas de estudiantes (1 a 25 siempre) ===
            for (int pos = 1; pos <= 25; pos++) {

                Row row = sheet.createRow(rowIndex++);

                // Determinar estilo de la fila completa
                CellStyle rowStyle = (pos <= 18) ? styleNormal : styleEspera;

                int c = 0;

                // Intentamos encontrar el estudiante que tenga ese número
                int finalPos = pos;
                EstudianteAsignacionDTO est = oferta.getListaEstudiantes()
                        .stream()
                        .filter(e -> e.getNumero() == finalPos)
                        .findFirst()
                        .orElse(null);

                // ========================================
                // Si no existe estudiante → llenar fila vacía
                // ========================================
                if (est == null) {
                    crearCell(row, c++, pos, rowStyle);    // Aun vacío, mostramos el número
                    crearCell(row, c++, "", rowStyle);
                    crearCell(row, c++, "", rowStyle);
                    crearCell(row, c++, "", rowStyle);
                    crearCell(row, c++, "", rowStyle);
                    crearCell(row, c, "", rowStyle); // % avance vacío
                    continue; // pasar a siguiente fila
                }

                // ========================================
                // Si existe → llenar datos del estudiante
                // ========================================

                crearCell(row, c++, est.getNumero(), rowStyle);
                crearCell(row, c++, est.getCodigo(), rowStyle);
                crearCell(row, c++, est.getApellidos(), rowStyle);
                crearCell(row, c++, est.getNombres(), rowStyle);
                crearCell(row, c++, est.getUsuario(), rowStyle);

                // % Avance
                Cell avanceCell = row.createCell(c);
                avanceCell.setCellValue(est.getPorcentajeAvance().doubleValue());

                // SOLO esta celda puede ser estiloNivelado
                if (est.isEsNivelado()) {

                    avanceCell.setCellStyle(styleNivelado4Decimales);
                } else {
                    // Crear estilo con 4 decimales
                    CellStyle style4Decimales = workbook.createCellStyle();
                    style4Decimales.cloneStyleFrom(rowStyle); // conservar bordes, alineación, etc.
                    style4Decimales.setDataFormat(df.getFormat("0.0000"));
                    avanceCell.setCellStyle(style4Decimales);
                }

            }


            // Línea en blanco entre una oferta y otra
            rowIndex++;
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Workbook generarReportePublicacion(Long periodoId) {
        // 1. Buscar el periodo del cual se generara el reporte
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        // 2. Validar estado GENERACION_LISTAS_PUBLICAS
        if (periodo.getEstado() != EstadoPeriodoAcademico.GENERACION_LISTAS_PUBLICAS) {
            throw new InvalidStateException(
                    "Solo se puede generar las lista publicas cuando el período está en estado GENERACION_LISTAS_PUBLICAS."
            );
        }
        // 3. Crear workbook (XSSF recomendado)
        Workbook workbook = new XSSFWorkbook();

        // 4. Obtener departamentos y ofertas
        List<DepartamentoReporteDTO> departamentos =
                consultaAsignacionService.generarListasDeAsigancionPorDepartamentos(periodoId);

        // 5. Crear una hoja por departamento
        for (DepartamentoReporteDTO dep : departamentos) {

            String nombreHoja = "Publicación " + dep.getNombre();
            if (nombreHoja.length() > 31) {
                nombreHoja = nombreHoja.substring(0, 31);
            }

            Sheet sheetDep = workbook.createSheet(nombreHoja);

            // --- usar el formato público (sin datos sensibles, en pares horizontalmente) ---
            crearHojaDepartamentoPublico(sheetDep, dep, workbook);
        }
        archivoService.guardarReportePublicacion(workbook, periodo);
        periodo.setEstado(EstadoPeriodoAcademico.ASIGNACION_PROCESADA);
        periodoAcademicoRepository.save(periodo);
        // 6. Retornar workbook para que el dominio de archivos lo guarde o lo devuelva
        return workbook;
    }


    /**
     * Crea una hoja pública para un departamento, ocultando datos sensibles y
     * mostrando solo código y posición. Además acomoda las ofertas en pares,
     * una al lado de la otra (dos columnas de tablas).
     */
    private void crearHojaDepartamentoPublico(Sheet sheet, DepartamentoReporteDTO dep, Workbook workbook) {
        // ===== Estilos =====
        CellStyle styleNormal = workbook.createCellStyle();
        ActivaBordesCeldas(styleNormal);

        CellStyle styleTitulo = crearColorConBorde(workbook, IndexedColors.GREY_25_PERCENT);
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        styleTitulo.setFont(boldFont);
        styleTitulo.setAlignment(HorizontalAlignment.CENTER);
        styleTitulo.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle styleEspera = crearColorConBorde(workbook, IndexedColors.BLUE_GREY);
        // Cada bloque tendrá 25 filas + 2 (título + espacio)
        int bloquesAltura = 27;

        int rowOffset = 0;      // fila desde donde inicia el siguiente par
        int colOffsetIzq = 0;   // bloque izquierdo (A)
        int colOffsetDer = 6;   // bloque derecho (H)

        List<OfertaReporteDTO> ofertas = dep.getOfertas();

        for (int i = 0; i < ofertas.size(); i += 2) {

            OfertaReporteDTO left = ofertas.get(i);
            OfertaReporteDTO right = (i + 1 < ofertas.size()) ? ofertas.get(i + 1) : null;

            // ============================
            // Oferta izquierda
            // ============================
            escribirBloquePublico(sheet, left, rowOffset, colOffsetIzq, styleTitulo, styleNormal, styleEspera);

            // ============================
            // Oferta derecha (si existe)
            // ============================
            if (right != null) {
                escribirBloquePublico(sheet, right, rowOffset, colOffsetDer, styleTitulo, styleNormal, styleEspera);
            }

            // Avanzamos a la siguiente fila de bloques
            rowOffset += bloquesAltura;
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < 11; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Escribe un bloque de oferta pública "compress" sin datos sensibles.
     */
    private void escribirBloquePublico(
            Sheet sheet,
            OfertaReporteDTO oferta,
            int rowOffset,
            int colOffset,
            CellStyle styleTitulo,
            CellStyle styleNormal,
            CellStyle styleEspera
    ) {

        // === Título (nombre de la electiva) ===
        Row rowTitulo = sheet.getRow(rowOffset);
        if (rowTitulo == null) rowTitulo = sheet.createRow(rowOffset);

        Cell tituloCell = rowTitulo.createCell(colOffset);
        tituloCell.setCellValue(oferta.getNombreElectiva());
        tituloCell.setCellStyle(styleTitulo);

        // Merge 5 columnas
        sheet.addMergedRegion(new CellRangeAddress(
                rowOffset, rowOffset, colOffset, colOffset + 4
        ));

        // === 25 filas ===
        for (int i = 1; i <= 25; i++) {

            Row row = sheet.getRow(rowOffset + i);

            if (row == null) row = sheet.createRow(rowOffset + i);

            int finalI = i;

            // Determinar estilo de la fila completa
            CellStyle rowStyle = (finalI <= 18) ? styleNormal : styleEspera;

            EstudianteAsignacionDTO est = oferta.getListaEstudiantes()
                    .stream()
                    .filter(e -> e.getNumero() == finalI)
                    .findFirst()
                    .orElse(null);

            int c = colOffset;

            // Número
            crearCell(row, c++, i, rowStyle);

            if (est != null) {
                crearCell(row, c++, est.getCodigo(), rowStyle);
                crearCell(row, c++, est.getApellidos(), rowStyle);
                crearCell(row, c++, est.getNombres(), rowStyle);
                crearCell(row, c, est.getUsuario(), rowStyle);

            } else {
                // Columnas vacías restantes
                crearCell(row, c++, "", rowStyle);
                crearCell(row, c++, "", rowStyle);
                crearCell(row, c++, "", rowStyle);
                crearCell(row, c, "", rowStyle);
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    public ReporteArchivoResponse obtenerArchivoReporteTecnico(Long periodoId) {
        validarEstadoParaDescarga(periodoId);

        CargaArchivo archivo = cargaArchivoRepository
                .findTopByPeriodoIdAndTipoArchivoOrderByFechaCargaDesc(periodoId, TipoArchivo.REPORTE_DETALLADO)
                .orElseThrow(() -> new ResourceNotFoundException("El reporte técnico no ha sido generado."));

        File file = new File(archivo.getRutaAlmacenamiento());

        if (!file.exists()) {
            throw new RuntimeException("El archivo del reporte técnico no existe en disco.");
        }

        ReporteArchivoResponse response = new ReporteArchivoResponse();
        response.setArchivo(new FileSystemResource(file));
        response.setNombreArchivo(archivo.getNombreArchivo());

        return response;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public ReporteArchivoResponse obtenerArchivoReportePublico(Long periodoId) {

        validarEstadoParaDescarga(periodoId);

        CargaArchivo registro = cargaArchivoRepository
                .findTopByPeriodoIdAndTipoArchivoOrderByFechaCargaDesc(
                        periodoId, TipoArchivo.LISTAS)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El reporte público no ha sido generado."
                ));

        File archivoFisico = new File(registro.getRutaAlmacenamiento());

        if (!archivoFisico.exists()) {
            throw new ResourceNotFoundException(
                    "El archivo del reporte público no existe en disco."
            );
        }

        ReporteArchivoResponse response = new ReporteArchivoResponse();
        response.setArchivo(new FileSystemResource(archivoFisico));
        response.setNombreArchivo(registro.getNombreArchivo());

        return response;
    }


    private void validarEstadoParaDescarga(Long periodoId) {

        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        if (periodo.getEstado() != EstadoPeriodoAcademico.ASIGNACION_PROCESADA &&
                periodo.getEstado() != EstadoPeriodoAcademico.CERRADO) {

            throw new InvalidStateException(
                    "No es posible descargar los reportes. " +
                            "El período debe estar en ASIGNACION_PROCESADA o CERRADO."
            );
        }

    }

    // -------- Utilidades --------
    /**
     * Aplica bordes negros delgados a todos los lados de una celda.
     *
     * <p>Este método estandariza el estilo visual de todas las celdas en el reporte,
     * garantizando que se presenten como una tabla con límites claros.</p>
     *
     * @param styleNormal Estilo al cual se aplicarán los bordes.
     */

    private void ActivaBordesCeldas(CellStyle styleNormal) {
        styleNormal.setBorderTop(BorderStyle.THIN);
        styleNormal.setBorderBottom(BorderStyle.THIN);
        styleNormal.setBorderLeft(BorderStyle.THIN);
        styleNormal.setBorderRight(BorderStyle.THIN);
        styleNormal.setTopBorderColor(IndexedColors.BLACK.getIndex());
        styleNormal.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        styleNormal.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        styleNormal.setRightBorderColor(IndexedColors.BLACK.getIndex());
    }

    private void ensureFolderExists(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            log.info("Carpeta creada: {} = {}", path, created);
        }
    }
    /**
     * Crea un estilo de celda con un color de fondo sólido y bordes negros.
     *
     * <p>Este estilo se usa para colorear celdas según el estado de asignación
     * (asignada, lista de espera, sin cupo, etc.).</p>
     *
     * @param workbook Libro Excel al que pertenece el estilo.
     * @param color    Color de fondo para la celda.
     * @return Estilo configurado con color y bordes.
     */
    private CellStyle crearColorConBorde(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        ActivaBordesCeldas(style);

        return style;
    }

    /**
     * Crea una celda para el encabezado en una fila dada y le asigna un estilo.
     *
     * @param row   Fila donde se creará la celda.
     * @param col   Índice de columna donde se ubicará la celda.
     * @param value Texto que mostrará la celda.
     * @param style Estilo a aplicar (normalmente estilo de encabezado).
     */
    private void crearHeaderCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * Crea una celda en una fila con un valor dinámico y estilo opcional.
     *
     * <p>El valor puede ser numérico, texto o nulo. En caso de ser número,
     * la celda se escribe como valor numérico dentro del Excel.</p>
     *
     * @param row   Fila donde se crea la celda.
     * @param col   Columna donde se ubicará la celda.
     * @param valor Valor a escribir (String, Number o null).
     * @param style Estilo a aplicar (puede ser null).
     */
    private void crearCell(Row row, int col, Object valor, CellStyle style) {
        Cell cell = row.createCell(col);
        if (valor instanceof Number num) cell.setCellValue(num.doubleValue());
        else if (valor != null) cell.setCellValue(valor.toString());
        else cell.setBlank();
        if (style != null) cell.setCellStyle(style);
    }
    /**
     * Busca dentro de la lista de asignaciones la que corresponde a un número
     * de opción específico.
     *
     * @param asignaciones Lista de asignaciones de electivas del estudiante.
     * @param opcion       Número de opción a buscar.
     * @return La asignación correspondiente o {@code null} si no existe.
     */
    private EstudianteAsignacionReporteResponse.AsignacionElectivaInfo buscarAsignacion(
            List<EstudianteAsignacionReporteResponse.AsignacionElectivaInfo> asignaciones,
            int opcion
    ) {
        if (asignaciones == null) return null;

        return asignaciones.stream()
                .filter(a -> a.getNumeroOpcion() != null && a.getNumeroOpcion() == opcion)
                .findFirst()
                .orElse(null);
    }

    /**
     * Devuelve el estilo que debe aplicarse según si el estudiante es nivelado o no.
     *
     * @param nivelado        Indica si el estudiante está nivelado.
     * @param niveladoStyle   Estilo para estudiantes nivelados.
     * @param normalStyle     Estilo estándar.
     * @return El estilo correspondiente según la condición.
     */
    private CellStyle estiloPorNivelado(boolean nivelado, CellStyle niveladoStyle, CellStyle normalStyle) {
        return nivelado ? niveladoStyle : normalStyle;
    }

    /**
     * Genera un mapa de estilos donde cada programa académico recibe un color único.
     *
     * <p>Los colores se reutilizan en caso de haber más programas que colores definidos.
     * Todos los estilos incluyen bordes negros y usan la misma fuente base.</p>
     *
     * @param workbook Libro Excel donde se crean los estilos.
     * @param ranking  Lista completa del ranking (se recorre para detectar programas).
     * @param normalFont Fuente estándar aplicada a los estilos generados.
     * @return Mapa donde la clave es el nombre del programa y el valor es su estilo asignado.
     */
    private Map<String, CellStyle> crearEstilosPrograma(
            Workbook workbook,
            List<EstudianteAsignacionReporteResponse> ranking,
            Font normalFont
    ) {
        Map<String, CellStyle> map = new HashMap<>();

        List<IndexedColors> paleta = List.of(
                IndexedColors.LIGHT_YELLOW,
                IndexedColors.LIGHT_GREEN,
                IndexedColors.LIGHT_CORNFLOWER_BLUE,
                IndexedColors.LIGHT_ORANGE,
                IndexedColors.LIGHT_TURQUOISE,
                IndexedColors.LIME,
                IndexedColors.TURQUOISE
        );

        AtomicInteger idx = new AtomicInteger(0);

        for (var est : ranking) {
            if (map.containsKey(est.getPrograma())) continue;

            IndexedColors color = paleta.get(idx.getAndIncrement() % paleta.size());

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(color.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            ActivaBordesCeldas(style);

            style.setFont(normalFont);

            map.put(est.getPrograma(), style);
        }

        return map;
    }



}
