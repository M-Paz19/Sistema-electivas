package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

// DTOs, Enums, Models
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.DatosAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.InconsistenciaDto;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.SimcaCargaResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioDesicionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoRespuestaFormulario;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.enums.EstadoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.enums.TipoArchivo;

// Repositorios
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.repository.DatosAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.plan_estudio.repository.PlanEstudioRepository;
import com.unicauca.fiet.sistema_electivas.archivo.repository.CargaArchivoRepository;

// Servicios y Mappers
import com.unicauca.fiet.sistema_electivas.archivo.service.ArchivoService;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.DatosAcademicoMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.RespuestaFormularioMapper;

// Excepciones
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;

// Imports de Spring y Java
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValidacionAcademicaServiceImpl implements ValidacionAcademicaService {

    // --- Dependencias Requeridas ---
    @Autowired
    private PeriodoAcademicoRepository periodoRepository;
    @Autowired
    private RespuestasFormularioRepository respuestasRepository;
    @Autowired
    private DatosAcademicoRepository datosAcademicoRepository;
    @Autowired
    private PlanEstudioRepository planEstudioRepository;
    @Autowired
    private CargaArchivoRepository cargaArchivoRepository;
    @Autowired
    private SimcaCsvParserService csvParserService;
    @Autowired
    private ArchivoService archivoService;
    @Autowired
    private DatosAcademicoMapper datosAcademicoMapper;
    // Nota: RespuestaFormularioMapper se usa de forma estática

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public SimcaCargaResponse cargarYValidarDatosSimca(Long idPeriodo, List<MultipartFile> archivos) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado"));

        if (periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_CONFIRMACION_SIMCA) {
            throw new InvalidStateException("Solo se pueden cargar datos SIMCA después de confirmar la lista (estado PROCESO_CONFIRMACION_SIMCA).");
        }

        // 1. Obtener los códigos que esperamos (los que se enviaron en lotes)
        List<String> codigosEsperados = respuestasRepository.findCodigosByPeriodoAndEstados(
                idPeriodo,
                List.of(EstadoRespuestaFormulario.CUMPLE, EstadoRespuestaFormulario.INCLUIDO)
        );
        Set<String> setCodigosEsperados = new HashSet<>(codigosEsperados);

        // Mapa para buscar la 'RespuestaFormulario' original por código
        Map<String, RespuestasFormulario> mapaRespuestas = respuestasRepository
                .findByPeriodoIdAndEstadoIn(idPeriodo, List.of(
                        EstadoRespuestaFormulario.CUMPLE,
                        EstadoRespuestaFormulario.INCLUIDO))
                .stream()
                .collect(Collectors.toMap(RespuestasFormulario::getCodigoEstudiante, r -> r));

        List<InconsistenciaDto> inconsistencias = new ArrayList<>();
        int registrosExitosos = 0;
        int archivosProcesados = 0;

        for (MultipartFile archivo : archivos) {

            String nombreArchivoGuardado = archivoService.guardarArchivo(archivo, TipoArchivo.DATOS_ACADEMICOS.name());

            CargaArchivo carga = new CargaArchivo();
            carga.setPeriodo(periodo);
            carga.setNombreArchivo(archivo.getOriginalFilename());
            carga.setRutaAlmacenamiento(nombreArchivoGuardado);
            carga.setFechaCarga(Instant.now());
            carga.setTipoArchivo(TipoArchivo.DATOS_ACADEMICOS);
            carga.setEstado(EstadoArchivo.PROCESADO);


            CargaArchivo cargaGuardada = cargaArchivoRepository.save(carga);

            List<DatosAcademico> datosDelArchivo;
            try {
                // 3. Parsear el CSV (HU 2.1.1.2)
                datosDelArchivo = csvParserService.parsearArchivoSimca(archivo);
                archivosProcesados++;
            } catch (Exception e) {
                // HU 2.1.1.2 Error de estructura
                throw new BusinessException("Error en el archivo '" + archivo.getOriginalFilename() + "'. " + e.getMessage());
            }

            List<DatosAcademico> aGuardar = new ArrayList<>();
            List<RespuestasFormulario> respuestasAActualizar = new ArrayList<>();

            for (DatosAcademico datos : datosDelArchivo) {
                String codigoCsv = datos.getCodigoEstudiante();

                // 4. Detección de inconsistencias (HU 2.1.1.3)
                if (!setCodigosEsperados.contains(codigoCsv)) {
                    inconsistencias.add(new InconsistenciaDto(
                            null, // No tenemos ID de respuesta formulario
                            codigoCsv,
                            datos.getNombres() + " " + datos.getApellidos(),
                            "Código no coincide o inactivo",
                            archivo.getOriginalFilename()
                    ));
                    // Marcar la respuesta si la encontramos por código
                    Optional<RespuestasFormulario> optRespuesta = respuestasRepository.findByCodigoEstudiante(codigoCsv);
                    optRespuesta.ifPresent(r -> {
                        r.setEstado(EstadoRespuestaFormulario.INCONSISTENTE_SIMCA);
                        respuestasAActualizar.add(r);
                    });
                } else {
                    // 5. Coincidencia encontrada (HU 2.1.1.1)
                    RespuestasFormulario respuestaOriginal = mapaRespuestas.get(codigoCsv);

                    // Buscar PlanEstudio por el nombre del programa
                    PlanEstudio plan = planEstudioRepository.findByPrograma_Nombre(datos.getPrograma())
                            .orElseThrow(() -> new BusinessException("No se encontró el Plan de Estudio para el programa: " + datos.getPrograma()));

                    datos.setPlanEstudios(plan);
                    datos.setArchivoCargado(cargaGuardada); // Vincular a la carga
                    // El parser ya asignó estado PENDIENTE_VALIDACION

                    aGuardar.add(datos);

                    // Actualizar estado de la respuesta original
                    respuestaOriginal.setEstado(EstadoRespuestaFormulario.DATOS_CARGADOS);
                    respuestasAActualizar.add(respuestaOriginal);
                    registrosExitosos++;
                }
            }
            datosAcademicoRepository.saveAll(aGuardar);
            respuestasRepository.saveAll(respuestasAActualizar);
        }

        // 6. Actualizar estado del período
        periodo.setEstado(EstadoPeriodoAcademico.PROCESO_CARGA_SIMCA);
        periodoRepository.save(periodo);

        // 7. Generar reporte (HU 2.1.1.1 y 2.1.1.4)
        String msg = String.format("Datos de SIMCA cargados exitosamente. Se procesaron %d registros de %d archivos.",
                registrosExitosos, archivosProcesados);

        return new SimcaCargaResponse(msg, archivosProcesados, registrosExitosos, inconsistencias.size(), inconsistencias);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public List<RespuestaFormularioResponse> obtenerInconsistencias(Long idPeriodo) {
        List<RespuestasFormulario> inconsistentes = respuestasRepository
                .findByPeriodoIdAndEstado(idPeriodo, EstadoRespuestaFormulario.INCONSISTENTE_SIMCA);

        return RespuestaFormularioMapper.toResponseList(inconsistentes);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public RespuestaFormularioDesicionResponse corregirCodigoEstudiante(Long respuestaId, String nuevoCodigo) {
        RespuestasFormulario respuesta = respuestasRepository.findById(respuestaId)
                .orElseThrow(() -> new ResourceNotFoundException("Respuesta no encontrada."));

        if (respuesta.getEstado() != EstadoRespuestaFormulario.INCONSISTENTE_SIMCA) {
            throw new InvalidStateException("Solo se pueden corregir respuestas con estado INCONSISTENTE_SIMCA.");
        }

        // Validar que el nuevo código no exista ya
        if (respuestasRepository.findByCodigoEstudiante(nuevoCodigo).isPresent()) {
            throw new BusinessException("El código " + nuevoCodigo + " ya existe en otra respuesta.");
        }

        // HU 2.1.2.1
        respuesta.setCodigoEstudiante(nuevoCodigo);
        respuesta.setEstado(EstadoRespuestaFormulario.CORREGIDO); // Listo para re-procesar
        respuestasRepository.save(respuesta);

        return RespuestaFormularioMapper.toRespuestaFormularioResponse(respuesta);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public RespuestaFormularioDesicionResponse tomarDecisionInconsistencia(Long respuestaId, boolean incluir) {
        RespuestasFormulario respuesta = respuestasRepository.findById(respuestaId)
                .orElseThrow(() -> new ResourceNotFoundException("Respuesta no encontrada."));

        if (respuesta.getEstado() != EstadoRespuestaFormulario.INCONSISTENTE_SIMCA) {
            throw new InvalidStateException("Solo se pueden tomar decisiones sobre respuestas con estado INCONSISTENTE_SIMCA.");
        }

        // HU 2.1.2.2
        if (incluir) {
            respuesta.setEstado(EstadoRespuestaFormulario.FORZAR_INCLUSION);
        } else {
            respuesta.setEstado(EstadoRespuestaFormulario.DESCARTADO);
        }
        respuestasRepository.save(respuesta);
        return RespuestaFormularioMapper.toRespuestaFormularioResponse(respuesta);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public String regenerarLoteCorregidos(Long idPeriodo) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado."));

        // HU 2.1.2.3: Generar lote solo con códigos corregidos o forzados
        List<String> codigosCorregidos = respuestasRepository.findCodigosByPeriodoAndEstados(
                idPeriodo,
                List.of(EstadoRespuestaFormulario.CORREGIDO, EstadoRespuestaFormulario.FORZAR_INCLUSION)
        );

        if (codigosCorregidos.isEmpty()) {
            throw new BusinessException("No hay códigos corregidos o forzados para generar un nuevo lote.");
        }

        List<List<String>> lotes = new ArrayList<>();
        lotes.add(codigosCorregidos);

        // Llamar al método sobrecargado con el sufijo
        archivoService.generarArchivosLotesSimca(lotes, periodo, "_CORREGIDOS");

        // Devolvemos el contenido del TXT
        return String.join("\n", codigosCorregidos);
    }


    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<DatosAcademicoResponse> preseleccionarNivelados(Long idPeriodo) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado"));

        // Validar que la carga SIMCA ya se haya hecho
        if (periodo.getEstado().ordinal() < EstadoPeriodoAcademico.PROCESO_CARGA_SIMCA.ordinal()) {
            throw new InvalidStateException("Debe cargar los datos de SIMCA antes de preseleccionar nivelados.");
        }

        // 1. Obtener todos los datos académicos cargados para este período
        List<DatosAcademico> todosLosDatos = datosAcademicoRepository.findByArchivoCargado_Periodo_Id(idPeriodo);

        List<DatosAcademico> posiblesNivelados = new ArrayList<>();

        for(DatosAcademico datos : todosLosDatos) {

            // HU 2.2.1.1: Preselección por créditos y período
            // "plan 'Ingeniería de Sistemas 2020' ... 8vo_semestre exigen min_creditos: 116"

            int semestres = datos.getPeriodosMatriculados();
            int creditos = datos.getCreditosAprobados();

            // Heurística simple (simulando la regla de 8vo semestre de la HU 2.2.1.1):
            // Si tiene 8 o más semestres Y 116 o más créditos, es candidato.
            boolean esCandidato = semestres >= 8 && creditos >= 116;

            if(esCandidato) {
                // HU 2.2.1.1: "El sistema debe marcar al estudiante como 'Posible Nivelado'"
                datos.setEstadoAptitud(EstadoAptitud.POSIBLE_NIVELADO);
                posiblesNivelados.add(datos);
            }
        }

        // Guardamos los cambios de estado (solo a los marcados)
        datosAcademicoRepository.saveAll(posiblesNivelados);

        // HU 2.2.1.2: "Generar un reporte/listado"
        return posiblesNivelados.stream()
                .map(datosAcademicoMapper::toResponse)
                .toList();
    }
}