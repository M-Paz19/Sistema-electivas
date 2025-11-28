package com.unicauca.fiet.sistema_electivas.periodo_academico.service;

import com.unicauca.fiet.sistema_electivas.archivo.service.ArchivoService;
import com.unicauca.fiet.sistema_electivas.common.exception.*;
import com.unicauca.fiet.sistema_electivas.electiva.model.ProgramaElectiva;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ProgramaElectivaRepository;
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
import com.unicauca.fiet.sistema_electivas.programa.dto.ProgramaResponse;
import com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import com.unicauca.fiet.sistema_electivas.programa.repository.ProgramaRepository;
import com.unicauca.fiet.sistema_electivas.programa.service.ProgramaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
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
    @Autowired
    private ProgramaElectivaRepository programaElectivaRepository;

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
    public CambioEstadoResponse abrirPeriodo(Long periodoId, boolean forzarApertura, Map<Long, Integer> opcionesPorPrograma){
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        if (!periodo.getEstado().equals(EstadoPeriodoAcademico.CONFIGURACION)) {
            throw new InvalidStateException("El período no está en estado CONFIGURACION");
        }

        // 1. Validar que el número de opciones esté configurado
        if ((periodo.getOpcionesPorPrograma() == null || periodo.getOpcionesPorPrograma().isEmpty())
                && (opcionesPorPrograma == null || opcionesPorPrograma.isEmpty())) {

            throw new BusinessException(
                    "Debe definir el número de opciones por programa antes de abrir el período."
            );
        }

        // 2. Validar oferta
        if (!ofertaRepository.hasOfertaValida(periodoId)) {
            throw new BusinessException("No se puede abrir el período. La oferta está vacía o mal configurada.");
        }
        //  Obtener electivas ofertadas del período actual
        List<Oferta> electivasOfertadas = ofertaRepository.findByPeriodo_Id(periodoId);

        // 3. Obtener los programas aprobados
        List<Programa> programasAprobados = programaRepository.findByEstado(EstadoPrograma.APROBADO);
        if (programasAprobados.isEmpty()) {
            throw new BusinessException("No se encontro programas aprobados");
        }


        // 4. Validar correctamente que el mapa venga bien configurado
        validarOpcionesPorPrograma(
                opcionesPorPrograma,
                programasAprobados,
                electivasOfertadas
        );

        // Guardar en el período
        periodo.setOpcionesPorPrograma(opcionesPorPrograma);



        // 5. Validar que no haya otro período activo
        if (periodoRepository.existsByEstadoIn(EstadoPeriodoAcademico.obtenerEstadosActivos())) {
            throw new InvalidStateException("Ya existe otro período académico activo en curso.");
        }

        // 6. Validar fecha de apertura
        Instant ahora = Instant.now();
        if (ahora.isBefore(periodo.getFechaApertura()) && !forzarApertura) {
            throw new InvalidStateException("La fecha actual es anterior a la fecha de apertura configurada.");
        }


        //7. Cambiar su estado a EN_CURSO
        electivasOfertadas.forEach(eo -> eo.setEstado(EstadoOferta.EN_CURSO));
        ofertaRepository.saveAll(electivasOfertadas);

        //8. Extraer las electivas asociadas (para el formulario)
        List<Electiva> electivasEnCurso = electivasOfertadas.stream()
                .map(Oferta::getElectiva)
                .collect(Collectors.toList());

        // 9. Crear formulario de preinscripción
        List<ProgramaElectiva> relaciones=programaElectivaRepository.findAllWithProgramaAprobadoAndElectiva();

        // Filtrar solo relaciones cuyas electivas estén en curso
        List<ProgramaElectiva> relacionesEnCurso = relaciones.stream()
                .filter(pe -> electivasEnCurso.contains(pe.getElectiva()))
                .toList();

        Map<Long, List<Electiva>> electivasPorPrograma = new HashMap<>();

        for (ProgramaElectiva pe : relacionesEnCurso) {
            electivasPorPrograma
                    .computeIfAbsent(pe.getPrograma().getId(), k -> new ArrayList<>())
                    .add(pe.getElectiva());
        }

        Map<String, Object> respuesta = googleFormsClient.generarFormulario(periodo, programasAprobados, electivasPorPrograma);

        periodo.setUrlFormulario((String) respuesta.get("url"));
        periodo.setFormId((String) respuesta.get("formId"));

        // 10. Cambiar estado del período a ABIERTO
        periodo.setEstado(EstadoPeriodoAcademico.ABIERTO_FORMULARIO);
        periodoRepository.save(periodo);

        // 11. Retornar respuesta
        return PeriodoAcademicoMapper.toCambioEstadoResponse(
                periodo,
                "Período " + periodo.getSemestre() +
                        " abierto exitosamente. Electivas activadas y formulario generado: " + respuesta.get("url")
        );

    }

    /**
     * Valida que el mapa de opciones por programa esté correctamente configurado
     * según los programas aprobados y la cantidad de electivas ofertadas.
     *
     * @param opcionesPorPrograma Mapa recibido para configurar el periodo (programaId -> opciones)
     * @param programasAprobados Lista de programas en estado APROBADO
     * @param electivasOfertadas Lista de electivas ofertadas del período
     */
    private void validarOpcionesPorPrograma(
            Map<Long, Integer> opcionesPorPrograma,
            List<Programa> programasAprobados,
            List<Oferta> electivasOfertadas
    ) {

        if (opcionesPorPrograma == null || opcionesPorPrograma.isEmpty()) {
            throw new BusinessException("Debe enviar las opciones por programa.");
        }

        // 1. Obtener IDs reales de los programas aprobados
        Set<Long> idsProgramasAprobados = programasAprobados.stream()
                .map(Programa::getId)
                .collect(Collectors.toSet());

        Set<Long> idsEnRequest = opcionesPorPrograma.keySet();

        // 2. Validar que vengan TODOS los programas aprobados
        if (!idsEnRequest.containsAll(idsProgramasAprobados)) {

            // obtener nombres de los que faltan
            List<String> faltantes = programasAprobados.stream()
                    .filter(p -> !idsEnRequest.contains(p.getId()))
                    .map(Programa::getNombre)
                    .toList();

            throw new BusinessException(
                    "El mapa opcionesPorPrograma no contiene todos los programas aprobados. " +
                            "Faltan los siguientes programas: " + faltantes
            );
        }

        // 3. Validar que no vengan programas extra
        if (!idsProgramasAprobados.containsAll(idsEnRequest)) {

            List<Long> idsSobrantes = idsEnRequest.stream()
                    .filter(id -> !idsProgramasAprobados.contains(id))
                    .toList();

            throw new BusinessException(
                    "El mapa opcionesPorPrograma contiene programas que no están aprobados. IDs no permitidos: " + idsSobrantes
            );
        }


        // 4. Validar que cada programa tenga al menos 1 opción
        opcionesPorPrograma.forEach((programaId, numeroOpciones) -> {
            if (numeroOpciones == null || numeroOpciones < 1) {
                throw new BusinessException(
                        "El programa con ID " + programaId + " debe tener al menos 1 opción."
                );
            }
        });

        // 5. Validar que el número de opciones no supere la oferta disponible
        int cantidadElectivas = electivasOfertadas.size();
        opcionesPorPrograma.forEach((programaId, numeroOpciones) -> {
            if (numeroOpciones > cantidadElectivas) {
                throw new BusinessException(
                        String.format(
                                "El programa %d tiene %d opciones, pero solo hay %d electivas ofertadas. " +
                                        "No puede asignar más opciones de las que existen.",
                                programaId, numeroOpciones, cantidadElectivas
                        )
                );
            }
        });
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

        String formId = periodo.getFormId();
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

        // Ejemplo: https://docs.google.com/forms/d/e/1maVuIHsSfEwHgmAGgtx0i7mdPZRm4JjUjiGNoc6EvCw/viewform
        // Resultado esperado: 1maVuIHsSfEwHgmAGgtx0i7mdPZRm4JjUjiGNoc6EvCw
        try {
            String[] partes = urlFormulario.split("/d/e/");
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CambioEstadoResponse cerrarPeriodoAcademico(Long periodoId) {

        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el período académico solicitado."));

        if (periodo.getEstado() != EstadoPeriodoAcademico.ASIGNACION_PROCESADA) {
            throw new InvalidStateException("El período no se encuentra en estado ASIGNACION_PROCESADA.");
        }

        // 1. Cambiar estado del período
        periodo.setEstado(EstadoPeriodoAcademico.CERRADO);
        periodoRepository.save(periodo);

        // 2. Actualizar todas las ofertas asociadas
        List<Oferta> ofertas = ofertaRepository.findByPeriodoId(periodoId);

        Instant ahora = Instant.now();
        for (Oferta oferta : ofertas) {
            oferta.setEstado(EstadoOferta.CERRADA);
            oferta.setFechaActualizacion(ahora);
        }

        ofertaRepository.saveAll(ofertas);

        // 3. Retornar respuesta
        return PeriodoAcademicoMapper.toCambioEstadoResponse(periodo,"El perido academico "+periodo.getSemestre()+" ha sido cerrada exitosamente.");
    }

}
