package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

// DTOs, Enums, Models
import com.unicauca.fiet.sistema_electivas.plan_estudio.enums.EstadoPlanEstudio;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.*;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.ValidacionProcesamientoMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoRespuestaFormulario;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;


// Repositorios
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.repository.DatosAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.plan_estudio.repository.PlanEstudioRepository;

// Servicios y Mappers
import com.unicauca.fiet.sistema_electivas.archivo.service.ArchivoService;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.DatosAcademicoMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.RespuestaFormularioMapper;

// Excepciones
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;

// Imports de Spring y Java
import com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import com.unicauca.fiet.sistema_electivas.programa.repository.ProgramaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.text.Normalizer;
import java.util.*;
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
    private SimcaCsvParserService csvParserService;
    @Autowired
    private ArchivoService archivoService;
    @Autowired
    private ProgramaRepository programaRepository;
    @Autowired
    private DatosAcademicoMapper datosAcademicoMapper;
    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public SimcaCargaResponse cargarYValidarDatosSimca(Long idPeriodo, MultipartFile[] archivos) {
        PeriodoAcademico periodo = validarPeriodoParaCarga(idPeriodo);

        // 1. Obtener respuestas esperadas y mapa de referencia
        Map<String, RespuestasFormulario> mapaRespuestas = obtenerMapaRespuestasValidas(idPeriodo);
        Set<String> codigosEsperados = mapaRespuestas.keySet();
        // Mapa para buscar la 'RespuestaFormulario' original por código

        List<InconsistenciaDto> inconsistencias = new ArrayList<>();
        int registrosExitosos = 0;
        int archivosProcesados = 0;

        for (MultipartFile archivo : archivos) {
            archivosProcesados++;
            registrosExitosos += procesarArchivoSimca(
                    archivo, periodo, mapaRespuestas,
                    codigosEsperados, inconsistencias
            );
        }
        verificarFaltantesSimca(mapaRespuestas, codigosEsperados, inconsistencias);
        // 7. Verificar si aún hay respuestas sin datos cargados
        String mensajeBase = String.format(
                "Se procesaron %d archivos con %d registros exitosos.",
                archivosProcesados, registrosExitosos
        );
        String mensajeFinal=actualizarEstadoPeriodoSiCompleto(periodo, idPeriodo);
        // 8. Generar reporte (HU 2.1.1.1 y 2.1.1.4)

        return new SimcaCargaResponse(mensajeBase+mensajeFinal, archivosProcesados, registrosExitosos, inconsistencias.size(), inconsistencias);
    }

    /**
     * Verifica que el período esté en un estado válido para iniciar la carga de datos desde SIMCA.
     *
     * @param idPeriodo ID del período académico
     * @return el objeto {@link PeriodoAcademico} validado
     * @throws ResourceNotFoundException si no existe el período
     * @throws InvalidStateException si el período no está en PROCESO_CONFIRMACION_SIMCA
     */
    private PeriodoAcademico validarPeriodoParaCarga(Long idPeriodo) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado"));
        if (periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_CONFIRMACION_SIMCA) {
            throw new InvalidStateException("Solo se pueden cargar datos SIMCA después de confirmar la lista.");
        }
        return periodo;
    }
    /**
     * Obtiene un mapa con las respuestas válidas (CUMPLE, INCLUIDO o DATOS_NO_CARGADOS)
     * del período académico, indexadas por código de estudiante.
     *
     * <p>Este mapa permite verificar rápidamente si un estudiante del archivo SIMCA
     * tiene una respuesta previa registrada en el sistema.</p>
     *
     * @param idPeriodo ID del período académico
     * @return mapa código-estudiante → {@link RespuestasFormulario}
     */
    private Map<String, RespuestasFormulario> obtenerMapaRespuestasValidas(Long idPeriodo) {
        List<RespuestasFormulario> respuestas = respuestasRepository.findByPeriodoIdAndEstadoIn(
                idPeriodo,
                List.of(
                        EstadoRespuestaFormulario.CUMPLE,
                        EstadoRespuestaFormulario.INCLUIDO,
                        EstadoRespuestaFormulario.DATOS_NO_CARGADOS,
                        EstadoRespuestaFormulario.PROGRAMA_NO_ENCONTRADO,
                        EstadoRespuestaFormulario.PLAN_NO_ENCONTRADO
                )
        );
        return respuestas.stream()
                .collect(Collectors.toMap(RespuestasFormulario::getCodigoEstudiante, r -> r));
    }

    /**
     * Procesa un archivo CSV de SIMCA, validando cada registro contra las respuestas esperadas
     * y registrando inconsistencias cuando sea necesario.
     *
     * <p>Guarda los datos académicos válidos y actualiza los estados de las respuestas
     * a DATOS_CARGADOS o INCONSISTENTE_SIMCA según corresponda.</p>
     *
     * @param archivo archivo CSV proveniente de SIMCA
     * @param periodo período académico en proceso
     * @param mapaRespuestas mapa de respuestas válidas (por código de estudiante)
     * @param codigosEsperados conjunto de códigos esperados según las respuestas previas
     * @param inconsistencias lista donde se agregan las inconsistencias detectadas
     * @return cantidad de registros cargados exitosamente
     */
    private int procesarArchivoSimca(
            MultipartFile archivo,
            PeriodoAcademico periodo,
            Map<String, RespuestasFormulario> mapaRespuestas,
            Set<String> codigosEsperados,
            List<InconsistenciaDto> inconsistencias
    ) {
        List<DatosAcademico> datosDelArchivo = leerArchivoSimca(archivo);
        CargaArchivo carga = archivoService.guardarArchivoDatosAcademicos(archivo, periodo);

        List<DatosAcademico> aGuardar = new ArrayList<>();
        List<RespuestasFormulario> respuestasAActualizar = new ArrayList<>();
        int registrosExitosos = 0;

        for (DatosAcademico datos : datosDelArchivo) {
            String codigo = datos.getCodigoEstudiante();

            if (!codigosEsperados.contains(codigo)) {
                registrarInconsistenciaNoInscrito(datos, archivo, inconsistencias);
                continue;
            }

            RespuestasFormulario respuesta = mapaRespuestas.get(codigo);

            if (esFilaIncompleta(datos)) {
                registrarInconsistenciaInactiva(datos, respuesta, archivo, inconsistencias);
                respuesta.setEstado(EstadoRespuestaFormulario.INCONSISTENTE_SIMCA);
                respuestasAActualizar.add(respuesta);
                // ELIMINAR DE ESPERADOS ya que ya se proceso inconsistencias
                codigosEsperados.remove(codigo);
                continue;
            }

            PlanEstudio plan = obtenerPlanParaDatosAcademicos(datos);
            if (plan == null) {

                // 1. Revisar por qué falló
                String normalizadoPrograma = Normalizer.normalize(datos.getPrograma(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .toLowerCase();

                Programa programaEncontrado = programaRepository.buscarFlexible(normalizadoPrograma, EstadoPrograma.APROBADO)
                        .orElse(null);

                if (programaEncontrado == null) {
                    // No existe un programa que coincida
                    respuesta.setEstado(EstadoRespuestaFormulario.PROGRAMA_NO_ENCONTRADO);
                    registrarInconsistenciaPrograma(datos, respuesta, archivo, inconsistencias);
                } else {
                    // Sí existe el programa, pero falló seleccionar plan
                    respuesta.setEstado(EstadoRespuestaFormulario.PLAN_NO_ENCONTRADO);
                    registrarInconsistenciaPlan(datos, respuesta, archivo, inconsistencias);
                }

                respuestasAActualizar.add(respuesta);
                // ELIMINAR DE ESPERADOS ya que ya se proceso inconsistencias
                codigosEsperados.remove(codigo);
                continue;
            }


            datos.setRespuesta(respuesta);
            datos.setPlanEstudios(plan);
            datos.setArchivoCargado(carga);
            aGuardar.add(datos);

            respuesta.setEstado(EstadoRespuestaFormulario.DATOS_CARGADOS);
            respuestasAActualizar.add(respuesta);

            // ELIMINAR DE ESPERADOS AL VALIDAR
            codigosEsperados.remove(codigo);

            registrosExitosos++;

        }

        datosAcademicoRepository.saveAll(aGuardar);
        respuestasRepository.saveAll(respuestasAActualizar);

        return registrosExitosos;
    }

    /**
     * Verifica los estudiantes esperados que no fueron encontrados en los archivos SIMCA cargados.
     *
     * <p>Los estudiantes sin registro en SIMCA son marcados con estado DATOS_NO_CARGADOS
     * y se registran como inconsistencias.</p>
     *
     * @param mapaRespuestas mapa de respuestas esperadas
     * @param codigosEsperados conjunto de códigos de estudiantes esperados
     * @param inconsistencias lista donde se agregan las inconsistencias detectadas
     */
    private void verificarFaltantesSimca(
            Map<String, RespuestasFormulario> mapaRespuestas,
            Set<String> codigosEsperados,
            List<InconsistenciaDto> inconsistencias
    ) {
        // IMPORTANTE: ahora solo quedan los faltantes reales
        for (String codigo : codigosEsperados) {

            RespuestasFormulario resp = mapaRespuestas.get(codigo);
            if (resp != null) {
                inconsistencias.add(new InconsistenciaDto(
                        resp.getId(),
                        codigo,
                        resp.getNombreEstudiante(),
                        "Se esperaba información de SIMCA para este estudiante, pero no se encontró en los archivos.",
                        null
                ));

                resp.setEstado(EstadoRespuestaFormulario.DATOS_NO_CARGADOS);
                respuestasRepository.save(resp);
            }
        }
    }

    /**
     * Verifica si el período académico puede avanzar de estado tras la carga de datos SIMCA.
     *
     * <p>Si ya no existen respuestas con estado DATOS_NO_CARGADOS ni INCONSISTENTE_SIMCA,
     * se actualiza el estado del período a PROCESO_CARGA_SIMCA.</p>
     *
     * @param periodo período académico evaluado
     * @param idPeriodo ID del período
     * @return mensaje informativo indicando si se puede continuar o si aún hay inconsistencias
     */
    private String actualizarEstadoPeriodoSiCompleto(PeriodoAcademico periodo, Long idPeriodo) {
        boolean existenPendientes = respuestasRepository.existsByPeriodoIdAndEstadoIn(
                idPeriodo,
                List.of(
                        EstadoRespuestaFormulario.DATOS_NO_CARGADOS,
                        EstadoRespuestaFormulario.INCONSISTENTE_SIMCA,
                        EstadoRespuestaFormulario.PROGRAMA_NO_ENCONTRADO,
                        EstadoRespuestaFormulario.PLAN_NO_ENCONTRADO
                )
        );
        String mensaje;
        if (!existenPendientes) {
            periodo.setEstado(EstadoPeriodoAcademico.PROCESO_CARGA_SIMCA);
            periodoRepository.save(periodo);
            mensaje = " Todos los datos de SIMCA cargados exitosamente, puede continuar con el proceso de validar nivelados.";

        }else{
            mensaje = " Sin embargo, aun se detectaron inconsistencias que deben ser resueltas " +
                    "antes de poder continuar con el proceso de validar nivelados.";
        }
        return mensaje;
    }


    /**
     * Busca y asigna el plan de estudios adecuado a partir de los datos académicos de SIMCA.
     * Ignora mayúsculas/tildes en la comparación del nombre del programa.
     *
     * @param datos datos académicos del estudiante
     * @return plan de estudio encontrado o null si no se pudo determinar
     */
    public PlanEstudio obtenerPlanParaDatosAcademicos(DatosAcademico datos) {
        // 1. Buscar programa activo directamente (ignorando tildes y mayúsculas)
        String normalizado = Normalizer
                .normalize(datos.getPrograma(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // elimina acentos
                .toLowerCase();
        Programa programa = programaRepository.buscarFlexible(normalizado, EstadoPrograma.APROBADO).orElse(null);
        System.out.println(programa);
        if (programa == null) return null; // No hay programa activo coincidente
        System.out.println("encontrado");
        // 3. Buscar planes activos del programa
        List<PlanEstudio> planesActivos = planEstudioRepository
                .findByProgramaAndEstado(programa, EstadoPlanEstudio.ACTIVO);

        if (planesActivos.isEmpty()) return null;

        if (planesActivos.size() == 1) return planesActivos.get(0);

        // 4. Si hay varios, determinar por año de ingreso
        String codigo = datos.getCodigoEstudiante();
        if (codigo.length() < 12) return null; // código no válido

        int anioIngreso = 2000 + Integer.parseInt(codigo.substring(4, 6));

        // Ordenar los planes por año de inicio
        List<PlanEstudio> planesOrdenados = planesActivos.stream()
                .sorted(Comparator.comparing(PlanEstudio::getAnioInicio))
                .toList();

        // Caso 1: si el año de ingreso es anterior a todos los planes → asignar el primero
        if (anioIngreso < planesOrdenados.get(0).getAnioInicio()) {
            return planesOrdenados.get(0);
        }

        // Caso 2: si el año de ingreso es posterior o igual al más reciente → asignar el último
        if (anioIngreso >= planesOrdenados.get(planesOrdenados.size() - 1).getAnioInicio()) {
            return planesOrdenados.get(planesOrdenados.size() - 1);
        }

        // Caso 3: buscar el plan cuyo rango contenga el año de ingreso
        for (int i = 0; i < planesOrdenados.size() - 1; i++) {
            PlanEstudio actual = planesOrdenados.get(i);
            PlanEstudio siguiente = planesOrdenados.get(i + 1);

            if (anioIngreso >= actual.getAnioInicio() && anioIngreso < siguiente.getAnioInicio()) {
                return actual;
            }
        }

        return null; // no debería llegar aquí normalmente
    }

    /**
     * Lee y convierte un archivo CSV de SIMCA en una lista de objetos {@link DatosAcademico}.
     *
     * @param archivo archivo CSV a procesar
     * @return lista de registros académicos
     * @throws BusinessException si ocurre un error al parsear el archivo
     */
    private List<DatosAcademico> leerArchivoSimca(MultipartFile archivo) {
        try {
            return csvParserService.parsearArchivoSimca(archivo);
        } catch (Exception e) {
            throw new BusinessException("Error en el archivo '" + archivo.getOriginalFilename() + "'. " + e.getMessage());
        }
    }

    /**
     * Determina si una fila del archivo SIMCA está incompleta o con datos faltantes.
     *
     * <p>Se consideran incompletas las filas sin nombres, apellidos, programa,
     * usuario o cualquier campo obligatorio (@NotNull) faltante en DatosAcademico.</p>
     *
     * @param d datos académicos del estudiante
     * @return {@code true} si la fila está incompleta, {@code false} en caso contrario
     */
    private boolean esFilaIncompleta(DatosAcademico d) {

        // Campos de texto obligatorios
        if (esNuloOVacio(d.getNombres()) ||
                esNuloOVacio(d.getApellidos()) ||
                esNuloOVacio(d.getPrograma()) ||
                esNuloOVacio(d.getUsuario())) {
            return true;
        }

        // Campos @NotNull obligatorios
        if (d.getCreditosAprobados() == null ||
                d.getPeriodosMatriculados() == null ||
                d.getPromedioCarrera() == null ||
                d.getAprobadas() == null) {
            return true;
        }

        return false;
    }

    /**
     * Verifica si un string es nulo o está vacío (incluye espacios).
     */
    private boolean esNuloOVacio(String s) {
        return s == null || s.isBlank();
    }

    /**
     * Registra una inconsistencia cuando un estudiante aparece en SIMCA
     * pero no tiene una respuesta válida en el sistema.
     *
     * @param d datos del estudiante detectado
     * @param a archivo de origen
     * @param incs lista donde se agregará la inconsistencia
     */
    private void registrarInconsistenciaNoInscrito(DatosAcademico d, MultipartFile a, List<InconsistenciaDto> incs) {
        incs.add(new InconsistenciaDto(
                null,
                d.getCodigoEstudiante(),
                d.getNombres() + " " + d.getApellidos(),
                "Estudiante no inscrito en el proceso (no registró una respuesta válida).",
                a.getOriginalFilename()
        ));
    }

    /**
     * Registra una inconsistencia para estudiantes inactivos o con información incompleta en SIMCA.
     *
     * <p>Además, cambia el estado de la respuesta a INCONSISTENTE_SIMCA.</p>
     *
     * @param d datos académicos del estudiante
     * @param r respuesta original asociada al estudiante
     * @param a archivo de origen
     * @param incs lista donde se agregará la inconsistencia
     */
    private void registrarInconsistenciaInactiva(DatosAcademico d, RespuestasFormulario r, MultipartFile a, List<InconsistenciaDto> incs) {
        incs.add(new InconsistenciaDto(
                r.getId(),
                d.getCodigoEstudiante(),
                (d.getNombres() + " " + d.getApellidos()).trim(),
                "El código aparece inactivo o sin información válida en SIMCA.",
                a.getOriginalFilename()
        ));
    }
    /**
     * Registra una inconsistencia cuando no se encuentra un plan de estudios activo
     * adecuado para el programa o el año de ingreso del estudiante.
     *
     * <p>El estado de la respuesta también se marca como INCONSISTENTE_SIMCA.</p>
     *
     * @param d datos académicos del estudiante
     * @param r respuesta original asociada al estudiante
     * @param a archivo de origen
     * @param incs lista donde se agregará la inconsistencia
     */
    private void registrarInconsistenciaPlan(DatosAcademico d, RespuestasFormulario r, MultipartFile a, List<InconsistenciaDto> incs) {
        incs.add(new InconsistenciaDto(
                r.getId(),
                d.getCodigoEstudiante(),
                d.getNombres() + " " + d.getApellidos(),
                "No se encontró un plan de estudio activo adecuado para el programa o año de ingreso.",
                a.getOriginalFilename()
        ));
    }
    /**
     * Registra una inconsistencia cuando no se encuentra un programa en el sistema
     * que coincida con el nombre reportado por SIMCA (ignorando tildes/mayúsculas).
     *
     * @param d datos académicos del estudiante (provenientes de SIMCA)
     * @param r respuesta original asociada al estudiante
     * @param a archivo de origen que generó la inconsistencia
     * @param incs lista donde se agregará la inconsistencia
     */
    private void registrarInconsistenciaPrograma(
            DatosAcademico d,
            RespuestasFormulario r,
            MultipartFile a,
            List<InconsistenciaDto> incs
    ) {
        incs.add(new InconsistenciaDto(
                r.getId(),
                d.getCodigoEstudiante(),
                d.getNombres() + " " + d.getApellidos(),
                "No se encontró un programa registrado que coincida con '" + d.getPrograma() + "' reportado por SIMCA.",
                a.getOriginalFilename()
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public List<DatosAcademicoResponse> obtenerDatosAcademicosPorPeriodo(
            Long periodoId,
            List<EstadoAptitud> estadosFiltro
    ) {
        List<DatosAcademico> entidades;

        // Si NO enviaron filtro → traer todos los estados
        if (estadosFiltro == null || estadosFiltro.isEmpty()) {
            entidades = datosAcademicoRepository.findByRespuesta_PeriodoId(periodoId);
        } else {
            entidades = datosAcademicoRepository.findByRespuesta_PeriodoIdAndEstadoAptitudIn(
                    periodoId,
                    estadosFiltro
            );
        }

        return datosAcademicoMapper.toResponseList(entidades);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public RespuestaFormularioDesicionResponse resolverInconsistenciaSimca(Long respuestaId, boolean corregir, @Nullable String nuevoCodigo) {
        RespuestasFormulario respuesta = respuestasRepository.findById(respuestaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la respuesta con ID " + respuestaId));

        if (respuesta.getEstado() != EstadoRespuestaFormulario.INCONSISTENTE_SIMCA) {
            throw new InvalidStateException("Solo se pueden resolver respuestas con estado INCONSISTENTE_SIMCA");
        }

        if (corregir) {
            if (nuevoCodigo == null || nuevoCodigo.isBlank()) {
                throw new BusinessException("Debe proporcionar el nuevo código del estudiante al corregir");
            }

            // Validar que no exista otra respuesta con el mismo código en el mismo periodo
            boolean existe = respuestasRepository.existsByPeriodoIdAndCodigoEstudianteAndEstado(
                    respuesta.getPeriodo().getId(),
                    nuevoCodigo.trim(),
                    EstadoRespuestaFormulario.DATOS_CARGADOS
            );

            if (existe) {
                throw new BusinessException("El código " + nuevoCodigo + " ya existe en otra respuesta del mismo período.");
            }

            // Actualizar código y marcar como pendiente de carga
            respuesta.setCodigoEstudiante(nuevoCodigo.trim());
            respuesta.setEstado(EstadoRespuestaFormulario.DATOS_NO_CARGADOS);

        } else {
            // Marcar como descartado por inconsistencia no corregida
            respuesta.setEstado(EstadoRespuestaFormulario.DESCARTADO_SIMCA);
        }

        respuestasRepository.save(respuesta);
        //  Verificar si ya se pueden avanzar los estados del período
        String mensajeEstado = "Respuesta actualizada correctamente"+ actualizarEstadoPeriodoSiCompleto(respuesta.getPeriodo(), respuesta.getPeriodo().getId());

        RespuestaFormularioDesicionResponse response = RespuestaFormularioMapper.toRespuestaFormularioResponse(respuesta);
        response.setMensaje(mensajeEstado); // si tu DTO lo soporta

        return response;
    }


    /**
     * {@inheritDoc}
     *
     */
    @Transactional(readOnly = true)
    @Override
    public List<RespuestaFormularioResponse> obtenerInconsistencias(Long idPeriodo) {
        List<RespuestasFormulario> inconsistentes = respuestasRepository
                .findByPeriodoIdAndEstadoIn(
                        idPeriodo,
                        List.of(
                                EstadoRespuestaFormulario.INCONSISTENTE_SIMCA,
                                EstadoRespuestaFormulario.DATOS_NO_CARGADOS,
                                EstadoRespuestaFormulario.PROGRAMA_NO_ENCONTRADO,
                                EstadoRespuestaFormulario.PLAN_NO_ENCONTRADO
                        )
                );
        return RespuestaFormularioMapper.toResponseList(inconsistentes);
    }


    /**
     * {@inheritDoc}
     *
     */
    @Transactional
    @Override
    public String regenerarLoteCorregidos(Long idPeriodo) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado."));

        // Seleccionar todos los códigos que deben reenviarse a SIMCA
        List<String> codigosPendientes = respuestasRepository.findCodigosByPeriodoAndEstados(
                idPeriodo,
                List.of(
                        EstadoRespuestaFormulario.DATOS_NO_CARGADOS,
                        EstadoRespuestaFormulario.PROGRAMA_NO_ENCONTRADO,
                        EstadoRespuestaFormulario.PLAN_NO_ENCONTRADO
                )
        );

        if (codigosPendientes.isEmpty()) {
            throw new BusinessException("No hay códigos corregidos o pendientes para generar un nuevo lote.");
        }


        // Dividir la lista de códigos pendientes en lotes de máximo 50
        List<List<String>> lotes = new ArrayList<>();
        for (int i = 0; i < codigosPendientes.size(); i += 50) {
            int fin = Math.min(i + 50, codigosPendientes.size());
            lotes.add(codigosPendientes.subList(i, fin));
        }
        // Generar sufijo único con fecha y hora actual
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String sufijo = "_CORREGIDOS_" + timestamp;

        // Generar el archivo TXT con sufijo único
        archivoService.generarArchivosLotesSimca(lotes, periodo, sufijo);

        // Devolver el contenido concatenado (para logging o previsualización)
        return String.join("\n", codigosPendientes);
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    @Transactional
    public CambioEstadoValidacionResponse calcularPorcentajeAvance(Long idPeriodo) {
        // 1 Buscar y validar el período académico
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo académico no encontrado."));

        // Validar que el período esté exactamente en el estado PROCESO_CALCULO_AVANCE
        if (periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_REVISION_POTENCIALES_NIVELADOS) {
            throw new InvalidStateException("Solo se puede calcular el porcentaje de avance cuando el período está en estado PROCESO_REVISION_POTENCIALES_NIVELADOS.");
        }

        // 2 Verificar que no existan registros con estado POSIBLE_NIVELADO
        boolean existenPosiblesNivelados = datosAcademicoRepository.existsByRespuesta_Periodo_IdAndEstadoAptitud(
                idPeriodo,
                EstadoAptitud.POSIBLE_NIVELADO
        );

        if (existenPosiblesNivelados) {
            throw new InvalidStateException("No se puede calcular el porcentaje de avance mientras existan registros con estado POSIBLE_NIVELADO en este período.");
        }

        // 3 Obtener todos los datos académicos del período con estados válidos
        List<DatosAcademico> datosPeriodo = datosAcademicoRepository
                .findByRespuesta_Periodo_IdAndEstadoAptitudIn(
                        idPeriodo,
                        List.of(
                                EstadoAptitud.PENDIENTE_VALIDACION,
                                EstadoAptitud.NIVELADO_CONFIRMADO,
                                EstadoAptitud.NIVELADO_DESCARTADO
                        )
                );

        if (datosPeriodo.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron registros académicos válidos para este período.");
        }

        // 4 Inicializar contadores del proceso
        int totalProcesados = 0;
        int nivelados = 0;
        int errores = 0;

        // 5 Procesar cada registro
        for (DatosAcademico dato : datosPeriodo) {
            try {
                BigDecimal porcentajeAvance;

                if (Boolean.TRUE.equals(dato.getEsNivelado())) {
                    // Caso NIVELADO → 100%
                    porcentajeAvance = BigDecimal.valueOf(100.00);
                    nivelados++;
                } else {
                    PlanEstudio plan = dato.getPlanEstudios();

                    Integer creditosTotales = plan.getCreditosTotalesPlan();
                    Integer creditosElectivas = plan.getElectivasRequeridas() != null
                            ? plan.getElectivasRequeridas() * 3
                            : 0;
                    Integer creditosTG = plan.getCreditosTrabajoGrado() != null
                            ? plan.getCreditosTrabajoGrado()
                            : 0;

                    if (creditosTotales == null || dato.getCreditosAprobados() == null || dato.getAprobadas() == null) {
                        errores++;
                        continue;
                    }

                    int totalAjustado = creditosTotales - creditosElectivas - creditosTG;
                    int creditosEstudianteAjustados = dato.getCreditosAprobados() - (dato.getAprobadas() * 3);

                    double porcentajeCalc = ((double) creditosEstudianteAjustados / totalAjustado) * 100.0;

                    porcentajeAvance = BigDecimal.valueOf(porcentajeCalc).setScale(4, RoundingMode.HALF_UP);

                    // asegurar que no supere 100
                    if (porcentajeAvance.compareTo(BigDecimal.valueOf(100)) > 0) {
                        porcentajeAvance = BigDecimal.valueOf(100);
                    }
                }

                dato.setPorcentajeAvance(porcentajeAvance);
                dato.setEstadoAptitud(EstadoAptitud.AVANCE_CALCULADO);
                datosAcademicoRepository.save(dato);
                totalProcesados++;

            } catch (Exception e) {
                errores++;
            }
        }
        periodo.setEstado(EstadoPeriodoAcademico.PROCESO_CALCULO_APTITUD);
        PeriodoAcademico periodoActualizado = periodoRepository.save(periodo);
        // 6 Crear y retornar resumen del proceso
        return ValidacionProcesamientoMapper.toCambioEstadoResponse(periodoActualizado,String.format(
                "Cálculo completado para %d estudiantes. %d nivelados (100%%). %d con error.",
                totalProcesados, nivelados, errores
        ));

    }
    /**
     * {@inheritDoc}
     *
     */
    @Override
    @Transactional
    public CambioEstadoValidacionResponse validarRequisitosGenerales(Long periodoId) {

        // 1. Buscar período académico
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo académico no encontrado."));

        // Validar estado PROCESO_CALCULO_APTITUD
        if (periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_CALCULO_APTITUD) {
            throw new InvalidStateException(
                    "Solo se puede validar los requisitos generales cuando el período está en estado PROCESO_CALCULO_APTITUD."
            );
        }

        // 2. Obtener todos los registros académicos ya con porcentaje calculado
        List<DatosAcademico> estudiantes = datosAcademicoRepository
                .findByRespuesta_Periodo_IdAndEstadoAptitudIn(
                        periodoId,
                        List.of(
                                EstadoAptitud.AVANCE_CALCULADO
                        )
                );

        if (estudiantes.isEmpty()) {
            throw new ResourceNotFoundException("No existen registros académicos en este período.");
        }

        // Contadores
        int totalAptos = 0;
        int totalNoAptos = 0;
        int errores = 0;

        // 3. Procesar uno por uno
        for (DatosAcademico dato : estudiantes) {
            try {
                boolean esApto;

                // 3.1 Regla: estudiante nivelado -> APTO automático
                if (Boolean.TRUE.equals(dato.getEsNivelado())) {
                    esApto = true;

                } else {
                    // 3.2 Validar porcentaje de avance
                    BigDecimal avance = dato.getPorcentajeAvance();
                    if (avance == null) {
                        // No debería ocurrir: se calculó en HU previa
                        errores++;
                        continue;
                    }

                    // 3.3 Resultado final
                    esApto = avance.compareTo(BigDecimal.valueOf(65)) >= 0;
                }

                // 3.4 Actualizar estado
                dato.setEstadoAptitud(
                        esApto ? EstadoAptitud.APTO : EstadoAptitud.NO_APTO
                );

                datosAcademicoRepository.save(dato);

                // Contabilizar
                if (esApto) totalAptos++;
                else totalNoAptos++;

            } catch (Exception e) {
                errores++;
            }
        }

        // 4. Actualizar estado del período
        periodo.setEstado(EstadoPeriodoAcademico.PROCESO_FILTRADO_NO_ELEGIBLES);
        PeriodoAcademico actualizado = periodoRepository.save(periodo);

        // 5. Construir respuesta
        String resumen = String.format(
                "Validación completada. %d aptos, %d no aptos, %d con error.",
                totalAptos, totalNoAptos, errores
        );

        return ValidacionProcesamientoMapper.toCambioEstadoResponse(actualizado, resumen);
    }
}