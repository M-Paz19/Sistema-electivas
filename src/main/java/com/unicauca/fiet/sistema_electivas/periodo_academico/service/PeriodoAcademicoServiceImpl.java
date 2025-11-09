package com.unicauca.fiet.sistema_electivas.periodo_academico.service;

import com.unicauca.fiet.sistema_electivas.archivo.service.ArchivoService;
import com.unicauca.fiet.sistema_electivas.common.exception.*;
import com.unicauca.fiet.sistema_electivas.integracion.google.GoogleFormsClient;
import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.*;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoOferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.periodo_academico.mapper.PeriodoAcademicoMapper;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.OfertaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import com.unicauca.fiet.sistema_electivas.programa.repository.ProgramaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeriodoAcademicoServiceImpl implements PeriodoAcademicoService {

    private static final Pattern SEMESTRE_PATTERN = Pattern.compile("^20\\d{2}-(1|2)$");
    @Autowired
    private PeriodoAcademicoRepository periodoRepository;
    @Autowired
    private OfertaRepository ofertaRepository;
    @Autowired
    private ProgramaRepository programaRepository;
    @Autowired
    private GoogleFormsClient googleFormsClient;
    @Autowired
    private ArchivoService archivoService;
    @Autowired
    private FormularioImportService formularioImportService;
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PeriodoAcademicoResponse crearPeriodo(CrearPeriodoAcademicoDTO dto) {

        // 1. Validar duplicado
        periodoRepository.findBySemestre(dto.getSemestre()).ifPresent(p -> {
            throw new DuplicateResourceException(
                    "El semestre [" + dto.getSemestre() + "] ya existe. Por favor, utilice otro identificador."
            );
        });

        // 2. Validar formato del semestre
        if (!SEMESTRE_PATTERN.matcher(dto.getSemestre()).matches()) {
            throw new BusinessException("El formato del semestre debe ser '20XX-1' o '20XX-2'.");
        }

        // 3. Validar fechas coherentes
        Instant apertura = dto.getFechaApertura();
        Instant cierre = dto.getFechaCierre();

        if (apertura.isAfter(cierre)) {
            throw new BusinessException("La fecha de apertura no puede ser posterior a la de cierre.");
        }

        // 4. Convertir a LocalDate para extraer año y mes
        ZoneId zona = ZoneId.systemDefault();
        LocalDate aperturaFecha = LocalDate.ofInstant(apertura, zona);
        LocalDate cierreFecha = LocalDate.ofInstant(cierre, zona);

        String[] partes = dto.getSemestre().split("-");
        int anio = Integer.parseInt(partes[0]);
        int semestreNum = Integer.parseInt(partes[1]);


        if (semestreNum == 1) {
            // Ejemplo: 2026-1 → Apertura desde julio 2025 hasta cierre junio 2026
            LocalDate inicioPermitido = LocalDate.of(anio - 1, 7, 1);
            LocalDate finPermitido = LocalDate.of(anio, 6, 30);

            if (aperturaFecha.isBefore(inicioPermitido) || cierreFecha.isAfter(finPermitido)) {
                throw new BusinessException(
                        String.format(
                                "Las fechas del semestre %d-%d deben estar entre %s y %s.",
                                anio, semestreNum,
                                inicioPermitido, finPermitido
                        )
                );
            }
        } else {
            // Ejemplo: 2026-2 → Apertura y cierre dentro del mismo año (enero a diciembre)
            LocalDate inicioPermitido = LocalDate.of(anio, 1, 1);
            LocalDate finPermitido = LocalDate.of(anio, 12, 31);

            if (aperturaFecha.isBefore(inicioPermitido) || cierreFecha.isAfter(finPermitido)) {
                throw new BusinessException(
                        String.format(
                                "Las fechas del semestre %d-%d deben estar entre %s y %s.",
                                anio, semestreNum,
                                inicioPermitido, finPermitido
                        )
                );
            }
        }
        // 6. Crear y guardar

        PeriodoAcademico nuevo = PeriodoAcademicoMapper.toEntity(dto);


        return PeriodoAcademicoMapper.toResponse(periodoRepository.save(nuevo));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<PeriodoAcademicoResponse> listarPeriodos(String semestreTexto, EstadoPeriodoAcademico estado) {
        List<PeriodoAcademico> periodos;

        if (semestreTexto != null && !semestreTexto.isBlank() && estado != null) {
            // Filtro combinado por texto y estado
            periodos = periodoRepository.buscarPorSemestreYEstado(semestreTexto, estado);
        } else if (semestreTexto != null && !semestreTexto.isBlank()) {
            // Filtro solo por texto
            periodos = periodoRepository.buscarPorSemestre(semestreTexto);
        } else if (estado != null) {
            // Filtro solo por estado
            periodos = periodoRepository.findByEstadoOrderBySemestreDesc(estado);
        } else {
            // Sin filtros → lista todo
            periodos = periodoRepository.findAll(Sort.by(Sort.Direction.DESC, "semestre"));
        }

        return periodos.stream()
                .map(PeriodoAcademicoMapper::toResponse)
                .collect(Collectors.toList());
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CambioEstadoResponse abrirPeriodo(Long periodoId, boolean forzarApertura, Integer numeroOpcionesFormulario){
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        if (!periodo.getEstado().equals(EstadoPeriodoAcademico.CONFIGURACION)) {
            throw new InvalidStateException("El período no está en estado CONFIGURACION");
        }

        // 1. Validar que el número de opciones esté configurado
        if (periodo.getNumeroOpcionesFormulario() == null && numeroOpcionesFormulario == null) {
            throw new BusinessException(
                    "Debe definir el número de opciones del formulario antes de abrir el período."
            );
        }

        // Si viene en la solicitud, actualiza el valor antes de abrir
        if (numeroOpcionesFormulario != null) {
            periodo.setNumeroOpcionesFormulario(numeroOpcionesFormulario);
        }

        // 2. Validar oferta
        if (!ofertaRepository.hasOfertaValida(periodoId)) {
            throw new BusinessException("No se puede abrir el período. La oferta está vacía o mal configurada.");
        }
        //  Obtener electivas ofertadas del período actual
        List<Oferta> electivasOfertadas = ofertaRepository.findByPeriodo_Id(periodoId);

        // 2. Validar cantidad de electivas ofertadas vs. opciones del formulario
        int numeroOpciones = periodo.getNumeroOpcionesFormulario();
        if (electivasOfertadas.size() < numeroOpciones) {
            throw new BusinessException(
                    String.format(
                            "No se puede abrir el período: hay %d electivas ofertadas pero el formulario requiere %d opciones. " +
                                    "Debe haber al menos tantas electivas ofertadas como opciones disponibles.",
                            electivasOfertadas.size(), numeroOpciones
                    )
            );
        }

        // 3️. Validar que no haya otro período activo
        if (periodoRepository.existsByEstadoIn(EstadoPeriodoAcademico.obtenerEstadosActivos())) {
            throw new InvalidStateException("Ya existe otro período académico activo en curso.");
        }

        // 4. Validar fecha de apertura
        Instant ahora = Instant.now();
        if (ahora.isBefore(periodo.getFechaApertura()) && !forzarApertura) {
            throw new InvalidStateException("La fecha actual es anterior a la fecha de apertura configurada.");
        }

        // 5. Obtener los programas aprobados
        List<Programa> programasAprobados = programaRepository.findByEstado(EstadoPrograma.APROBADO);
        if (programasAprobados.isEmpty()) {
            throw new BusinessException("No se encontro programas aprobados");
        }

        //6. Cambiar su estado a EN_CURSO
        electivasOfertadas.forEach(eo -> eo.setEstado(EstadoOferta.EN_CURSO));
        ofertaRepository.saveAll(electivasOfertadas);

        //7. Extraer las electivas asociadas (para el formulario)
        List<Electiva> electivasEnCurso = electivasOfertadas.stream()
                .map(Oferta::getElectiva)
                .collect(Collectors.toList());


        // 8. Crear formulario de preinscripción
        String urlFormulario = googleFormsClient.crearFormulario(periodo, programasAprobados, electivasEnCurso);
        periodo.setUrlFormulario(urlFormulario);

        // 9. Cambiar estado del período a ABIERTO
        periodo.setEstado(EstadoPeriodoAcademico.ABIERTO_FORMULARIO);
        periodoRepository.save(periodo);

        // 10. Retornar respuesta
        return PeriodoAcademicoMapper.toCambioEstadoResponse(
                periodo,
                "Período " + periodo.getSemestre() +
                        " abierto exitosamente. Electivas activadas y formulario generado: " + urlFormulario
        );

    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CambioEstadoResponse cerrarFormulario(Long periodoId) {
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        if (periodo.getEstado() != EstadoPeriodoAcademico.ABIERTO_FORMULARIO) {
            throw new InvalidStateException("Solo se puede cerrar un período en estado ABIERTO.");
        }

        String formId = extraerFormId(periodo.getUrlFormulario());
        if (formId == null || formId.isEmpty()) {
            throw new InvalidStateException("No se puede cerrar el formulario: no se ha configurado un URL válido.");
        }

        // 1. Cerrar formulario en Google
        try {
            // flujo automático
            googleFormsClient.cerrarFormulario(formId);
            List<Map<String, String>> datos = googleFormsClient.obtenerRespuestas(formId);
            if (datos.isEmpty()) throw new GoogleFormsException("No se encontraron respuestas.");

            CargaArchivo archivo = archivoService.guardarArchivoRespuestas(datos, periodo);
            formularioImportService.procesarRespuestas(datos, periodo, archivo);

        } catch (GoogleFormsException ex) {
            // Fallback: solicitar carga manual
            log.warn("Fallo en obtención automática de respuestas. Requiere carga manual. Causa: {}", ex.getMessage());
            throw new InvalidStateException("No fue posible obtener respuestas automáticamente. Por favor cargue manualmente las respuestas.");
        }

        // 4. Cambiar estado del período
        periodo.setEstado(EstadoPeriodoAcademico.CERRADO_FORMULARIO);
        periodoRepository.save(periodo);
        // 10. Retornar respuesta
        return PeriodoAcademicoMapper.toCambioEstadoResponse(
                periodo,
                "Período " + periodo.getSemestre() +
                        " formulario cerrado exitosamente. Electivas en curso de asignación, formulario cerrado y respuestas guardadas "
        );
    }
    /**
     * Extrae el identificador único ({@code formId}) de un formulario de Google Forms
     * a partir de su URL completa.
     *
     * @param urlFormulario URL completa del formulario de Google.
     * @return El {@code formId} extraído de la URL.
     * @throws BusinessException Si la URL es nula, vacía o tiene un formato inválido.
     */
    private String extraerFormId(String urlFormulario) {
        if (urlFormulario == null || urlFormulario.isBlank()) {
            throw new BusinessException("La URL del formulario no puede ser nula o vacía");
        }

        // Ejemplo: https://docs.google.com/forms/d/1maVuIHsSfEwHgmAGgtx0i7mdPZRm4JjUjiGNoc6EvCw/viewform
        // Resultado esperado: 1maVuIHsSfEwHgmAGgtx0i7mdPZRm4JjUjiGNoc6EvCw
        try {
            String[] partes = urlFormulario.split("/d/");
            if (partes.length < 2) throw new BusinessException("URL inválida");

            String resto = partes[1];
            return resto.split("/")[0];
        } catch (Exception e) {
            throw new BusinessException("No se pudo extraer el formId de la URL: " + urlFormulario, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CambioEstadoResponse cargarRespuestasManual(Long periodoId, MultipartFile file) {
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        if (periodo.getEstado() != EstadoPeriodoAcademico.ABIERTO_FORMULARIO) {
            throw new InvalidStateException("Solo se pueden cargar respuestas de un período en estado ABIERTO.");
        }

        List<Map<String, String>> datos =  formularioImportService.parsearRespuestasFormulario(file, periodo);

        // Guardar archivo físicamente (como respaldo)
        CargaArchivo archivo = archivoService.guardarArchivoRespuestas(datos, periodo);

        // Procesar y guardar en BD
        formularioImportService.procesarRespuestas(datos, periodo, archivo);

        // Cambiar estado
        periodo.setEstado(EstadoPeriodoAcademico.CERRADO_FORMULARIO);
        periodoRepository.save(periodo);

        return PeriodoAcademicoMapper.toCambioEstadoResponse(
                periodo,
                "Período " + periodo.getSemestre() + " cerrado y respuestas cargadas manualmente."
        );
    }


}
