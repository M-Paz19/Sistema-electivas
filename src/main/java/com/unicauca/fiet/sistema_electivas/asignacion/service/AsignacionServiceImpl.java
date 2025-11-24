package com.unicauca.fiet.sistema_electivas.asignacion.service;

import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import com.unicauca.fiet.sistema_electivas.asignacion.enums.ResultadoAsignacion;


import com.unicauca.fiet.sistema_electivas.asignacion.model.AsignacionElectiva;
import com.unicauca.fiet.sistema_electivas.asignacion.repository.AsignacionElectivaRepository;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoRespuestaFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestaOpcion;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.OfertaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;


import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestaOpcionRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CambioEstadoValidacionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.ValidacionProcesamientoMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.repository.DatosAcademicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsignacionServiceImpl implements AsignacionService {
    @Autowired
    private PeriodoAcademicoRepository periodoRepository;
    @Autowired
    private DatosAcademicoRepository datosAcademicoRepository;
    @Autowired
    private RespuestaOpcionRepository respuestaOpcionRepository;
    @Autowired
    private RespuestasFormularioRepository respuestasFormularioRepository;
    @Autowired
    private AsignacionElectivaRepository asignacionElectivaRepository;
    @Autowired
    private OfertaRepository ofertaRepository;
    @Autowired
    private ConsultaAsignacionService consultaAsignacionService;
    @Autowired
    private ReglasElectivasService reglasElectivasServiceImpl;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CambioEstadoValidacionResponse filtrarEstudiantesNoElegibles(Long periodoId) {

        // 1. Buscar período
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período académico no encontrado."));

        // 2. Validar estado PROCESO_FILTRADO_NO_ELEGIBLES
        if (periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_FILTRADO_NO_ELEGIBLES) {
            throw new InvalidStateException(
                    "Solo se puede filtrar estudiantes no elegibles cuando el período está en estado PROCESO_FILTRADO_NO_ELEGIBLES."
            );
        }

        // 3. Obtener todos los estudiantes APTO
        List<DatosAcademico> aptos = datosAcademicoRepository
                .findByRespuesta_Periodo_IdAndEstadoAptitudIn(
                        periodoId,
                        List.of(EstadoAptitud.APTO)
                );

        if (aptos.isEmpty()) {
            throw new ResourceNotFoundException("No existen estudiantes aptos para filtrar.");
        }

        int totalExcluidos = 0;
        int errores = 0;

        // 4. Procesar cada estudiante
        for (DatosAcademico dato : aptos) {

            try {
                int electivasCursadas = dato.getAprobadas();
                int totalElectivasPlan = dato.getPlanEstudios().getElectivasRequeridas();

                boolean yaCompletoElectivas = electivasCursadas >= totalElectivasPlan;

                if (yaCompletoElectivas) {

                    dato.setEstadoAptitud(EstadoAptitud.EXCLUIDO_POR_ELECTIVAS);

                    datosAcademicoRepository.save(dato);
                    totalExcluidos++;
                }

            } catch (Exception e) {
                errores++;
            }
        }

        // 5. ACTUALIZAR ESTADO → EN_PROCESO_ASIGNACION
        periodo.setEstado(EstadoPeriodoAcademico.EN_PROCESO_ASIGNACION);
        PeriodoAcademico actualizado = periodoRepository.save(periodo);

        // 6. Construir mensaje
        String mensaje = String.format(
                "Filtrado completado. %d estudiantes fueron excluidos por haber cursado todas las electivas. Errores: %d",
                totalExcluidos, errores
        );

        return ValidacionProcesamientoMapper.toCambioEstadoResponse(actualizado, mensaje);
    }


    // ------------------------------------------------------------
    // 1. MÉTODO PRINCIPAL: PROCESO MASIVO
    // ------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Transactional
    public CambioEstadoValidacionResponse procesarAsignacionMasiva(Long periodoId) {
        // 1. Obtener y validar período
        PeriodoAcademico periodo = obtenerPeriodoValido(periodoId);

        // 2. Preparar datos en memoria necesarios para la asignación
        List<DatosAcademico> aptos = consultaAsignacionService.obtenerAptosOrdenadosInterno(periodoId);
        if (aptos.isEmpty()) {
            throw new InvalidStateException("No hay estudiantes aptos para asignar en este periodo.");
        }
        Map<String, RespuestasFormulario> respuestasPorEstudiante = mapearRespuestasPorEstudiante(periodoId);
        Map<Long, List<RespuestaOpcion>> opcionesPorRespuestaId = mapearOpcionesPorRespuesta(periodoId);
        List<Oferta> ofertas = ofertaRepository.findByPeriodoId(periodoId);
        Map<Long, Map<Long, AtomicInteger>> cuposRestantesPorOferta = inicializarCuposPorOferta(ofertas);
        Map<Long, AtomicInteger> listaEsperaPorOferta = inicializarListaEspera(ofertas);

        /**Posible uso futuro si necesito mas optimizacion
        Map<Long, PlanEstudio> planesPorId = planEstudioRepository.findAll()
                .stream()
                .collect(Collectors.toMap(PlanEstudio::getId, p -> p));
        **/
        // Lista global que acumula TODAS las asignaciones de TODOS los estudiantes
        List<AsignacionElectiva> acumuladoAsignaciones = new ArrayList<>();
        int procesados = 0;
        int errores = 0;

        // 3. Procesar cada estudiante
        for (DatosAcademico estudiante : aptos) {
            try {
                List<AsignacionElectiva> asignacionesEst= procesarUnEstudiante(
                        estudiante,
                        cuposRestantesPorOferta,
                        respuestasPorEstudiante,
                        opcionesPorRespuestaId,
                        listaEsperaPorOferta
                );
                acumuladoAsignaciones.addAll(asignacionesEst);
                procesados++;
            } catch (Exception ex) {
                estudiante.setEstadoAptitud(EstadoAptitud.ASIGNACION_ERROR);
                errores++;
                log.error("Error procesando estudiante {}: {}",
                        estudiante.getCodigoEstudiante(), ex.getMessage());
            }
        }

        // 4. Guardar cambios y actualizar estado del periodo
        datosAcademicoRepository.saveAll(aptos);
        // Guardar asignaciones si hubo
        if (!acumuladoAsignaciones.isEmpty()) {
            asignacionElectivaRepository.saveAll(acumuladoAsignaciones);
        }
        periodo.setEstado(EstadoPeriodoAcademico.ASIGNACION_PROCESADA);
        PeriodoAcademico actualizado = periodoRepository.save(periodo);
        // 5. Construir mensaje final
        String mensaje = String.format("Asignación completada. Estudiantes procesados: %d. Errores: %d.", procesados, errores);
        return ValidacionProcesamientoMapper.toCambioEstadoResponse(actualizado, mensaje);
    }

    private PeriodoAcademico obtenerPeriodoValido(Long periodoId) {
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el período académico con id=" + periodoId));

        if (periodo.getEstado() != EstadoPeriodoAcademico.EN_PROCESO_ASIGNACION) {
            throw new InvalidStateException(
                    "Solo se puede realizar el proceso de asignación cuando el período está en estado EN_PROCESO_ASIGNACION."
            );
        }
        return periodo;
    }

    // ------------------------------------------------------------
    // 2. MÉTODOS DE VALIDACIÓN Y MAPEO
    // ------------------------------------------------------------
    /**
     * Obtiene un mapa de respuestas de formulario de los estudiantes para un período dado.
     *
     * <p>La clave del mapa es el código del estudiante y el valor es el objeto
     * {@link RespuestasFormulario} correspondiente. Solo se incluyen respuestas
     * con estado {@code DATOS_CARGADOS}.</p>
     *
     * @param periodoId ID del período académico.
     * @return Mapa de código de estudiante a su respuesta de formulario.
     */
    private Map<String, RespuestasFormulario> mapearRespuestasPorEstudiante(Long periodoId) {
        return respuestasFormularioRepository
                .findByPeriodoIdAndEstado(periodoId, EstadoRespuestaFormulario.DATOS_CARGADOS)
                .stream()
                .collect(Collectors.toMap(RespuestasFormulario::getCodigoEstudiante, r -> r));
    }


    /**
     * Obtiene un mapa de opciones de respuesta agrupadas por ID de respuesta.
     *
     * <p>Para cada respuesta, las opciones se ordenan por su número de opción
     * ({@code opcionNum}) de forma ascendente.</p>
     *
     * @param periodoId ID del período académico.
     * @return Mapa de ID de respuesta a lista de opciones ordenadas.
     */
    private Map<Long, List<RespuestaOpcion>> mapearOpcionesPorRespuesta(Long periodoId) {
        List<RespuestaOpcion> todasLasOpciones = respuestaOpcionRepository.findAllOpcionesByPeriodoAndEstadoDatosCargados(periodoId);
        return todasLasOpciones.stream().collect(Collectors.groupingBy(
                o -> o.getRespuesta().getId(),
                Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> list.stream()
                                .sorted(Comparator.comparingInt(RespuestaOpcion::getOpcionNum))
                                .collect(Collectors.toList())
                )
        ));
    }

    /**
     * Inicializa en memoria los cupos restantes por oferta y por programa.
     *
     * <p>Devuelve un mapa donde la clave es el ID de la oferta y el valor es otro mapa
     * de programa a cupos restantes ({@link AtomicInteger}). Se utiliza {@link ConcurrentHashMap}
     * para permitir modificaciones concurrentes durante el proceso de asignación.</p>
     *
     * @param ofertas Lista de ofertas disponibles en el período.
     * @return Mapa de ID de oferta a mapa de ID de programa y cupos restantes.
     */
    private Map<Long, Map<Long, AtomicInteger>> inicializarCuposPorOferta(List<Oferta> ofertas) {
        Map<Long, Map<Long, AtomicInteger>> cuposRestantesPorOferta = new ConcurrentHashMap<>();
        for (Oferta oferta : ofertas) {
            Map<Long, AtomicInteger> cuposPorPrograma = new ConcurrentHashMap<>();
            oferta.getCuposPorPrograma().forEach((programaId, cupos) -> cuposPorPrograma.put(programaId, new AtomicInteger(cupos)));
            cuposRestantesPorOferta.put(oferta.getId(), cuposPorPrograma);
        }
        return cuposRestantesPorOferta;
    }

    /**
     * Inicializa en memoria los contadores de lista de espera para cada oferta.
     *
     * <p>Devuelve un mapa donde la clave es el ID de la oferta y el valor es un
     * {@link AtomicInteger} que representa la cantidad de estudiantes en lista de espera,
     * inicialmente en cero.</p>
     *
     * @param ofertas Lista de ofertas disponibles en el período.
     * @return Mapa de ID de oferta a contador de lista de espera.
     */
    private Map<Long, AtomicInteger> inicializarListaEspera(List<Oferta> ofertas) {
        Map<Long, AtomicInteger> listaEsperaPorOferta = new ConcurrentHashMap<>();
        for (Oferta oferta : ofertas) {
            listaEsperaPorOferta.put(oferta.getId(), new AtomicInteger(0));
        }
        return listaEsperaPorOferta;
    }
    // ------------------------------------------------------------
    // 3. PROCESAMIENTO POR ESTUDIANTE
    // ------------------------------------------------------------
    /**
     * Procesa la asignación de electivas para un estudiante individualmente.
     *
     * <p>Este método ejecuta todo el flujo de asignación oficial:
     * <ol>
     *     <li>Calcula cuántas electivas le corresponden al estudiante usando las reglas oficiales.</li>
     *     <li>Obtiene la respuesta del formulario del estudiante y valida que exista.</li>
     *     <li>Ordena las opciones de electivas proporcionadas por el estudiante según su prioridad.</li>
     *     <li>Crea las asignaciones base en memoria para cada opción.</li>
     *     <li>Realiza el primer recorrido de asignación directa, respetando los cupos por oferta y programa.</li>
     *     <li>Si quedan electivas pendientes, intenta asignarlas mediante la lista de espera.</li>
     *     <li>Guarda todas las asignaciones finales en la base de datos.</li>
     *     <li>Actualiza el estado del estudiante a {@code ASIGNACION_PROCESADA} o {@code ASIGNACION_ERROR} según corresponda.</li>
     * </ol>
     *
     * @param estudiante Datos académicos del estudiante a procesar.
     * @param cuposRestantesPorOferta Mapa en memoria con los cupos disponibles por oferta y programa.
     * @param respuestasPorEstudiante Mapa de respuestas de formulario por código de estudiante.
     * @param opcionesPorRespuestaId Mapa de opciones de electivas agrupadas por ID de respuesta.
     * @param listaEsperaPorOferta Contador en memoria de estudiantes en lista de espera por oferta.
     */
    private List<AsignacionElectiva> procesarUnEstudiante(
            DatosAcademico estudiante,
            Map<Long, Map<Long, AtomicInteger>> cuposRestantesPorOferta,
            Map<String, RespuestasFormulario> respuestasPorEstudiante,
            Map<Long, List<RespuestaOpcion>> opcionesPorRespuestaId,
            Map<Long, AtomicInteger> listaEsperaPorOferta
    ) {

        // 1. Calcular cuántas electivas merece
        int electivasAAsignar = reglasElectivasServiceImpl.calcularCantidadElectivasAAsignar(estudiante);

        // 2. Obtener respuesta del formulario
        RespuestasFormulario respuesta = respuestasPorEstudiante.get(estudiante.getCodigoEstudiante());
        if (respuesta == null) {
            estudiante.setEstadoAptitud(EstadoAptitud.ASIGNACION_ERROR);
            return List.of();
        }

        // 3. Obtener y ordenar sus opciones
        List<RespuestaOpcion> opcionesOriginal = opcionesPorRespuestaId.getOrDefault(respuesta.getId(), List.of());

        List<RespuestaOpcion> opcionesDelEstudiante = opcionesOriginal.stream()
                .sorted(Comparator.comparing(RespuestaOpcion::getOpcionNum))
                .toList();

        // 4. Crear las asignaciones base (en memoria)
        List<AsignacionElectiva> asignacionesDelEstudiante = opcionesDelEstudiante.stream()
                .map(op -> crearAsignacionBase(estudiante, op))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 5. Primer recorrido: asignación directa
        int asignadasDirectas = procesarAsignacionesDirectas(
                estudiante,
                electivasAAsignar,
                cuposRestantesPorOferta,
                opcionesDelEstudiante,
                asignacionesDelEstudiante
        );

        // 6. Electivas faltantes → intentar lista de espera
        int faltantes = (electivasAAsignar - asignadasDirectas) * 2;
        if (faltantes > 0) {
            procesarListasDeEspera(faltantes, asignacionesDelEstudiante, listaEsperaPorOferta);
        }

        // 7. Cambiar estado del estudiante
        estudiante.setEstadoAptitud(EstadoAptitud.ASIGNACION_PROCESADA);
        //8.Devolver las asignaciones hechas
        return asignacionesDelEstudiante;
    }

    // ------------------------------------------------------------
    // 4. MÉTODOS AUXILIARES DE ASIGNACIÓN
    // ------------------------------------------------------------
    /**
     * Crea una asignación base en memoria para un estudiante y una opción de electiva.
     * Inicializa el estado como NO_EVALUADA y registra la fecha de creación.
     *
     * @param estudiante Datos académicos del estudiante.
     * @param opcion Opción de electiva seleccionada por el estudiante.
     * @return Objeto AsignacionElectiva inicializado.
     */
    private AsignacionElectiva crearAsignacionBase(
            DatosAcademico estudiante,
            RespuestaOpcion opcion
    ) {
        // Si el estudiante dejó vacía esta opción → NO crear asignación
        if (opcion.getOferta() == null) {
            return null;
        }

        AsignacionElectiva a = new AsignacionElectiva();
        a.setEstudianteCodigo(estudiante.getCodigoEstudiante());
        a.setNumeroOpcion(opcion.getOpcionNum());
        a.setOferta(opcion.getOferta());
        a.setEstadoAsignacion(EstadoAsignacion.NO_EVALUADA);
        a.setFechaAsignacion(Instant.now());

        return a;
    }


    /**
     * Recorre las opciones de un estudiante e intenta asignarlas directamente,
     * respetando la cantidad de electivas a asignar y los cupos disponibles.
     *
     * @param estudiante Datos académicos del estudiante.
     * @param electivasAAsignar Cantidad de electivas que se deben asignar.
     * @param cuposRestantesPorOferta Map de cupos disponibles por oferta y programa.
     * @param opcionesDelEstudiante Lista de opciones de electiva del estudiante.
     * @param asignacionesDelEstudiante Lista de asignaciones base a actualizar.
     * @return Número de electivas efectivamente asignadas directamente.
     */
    private int procesarAsignacionesDirectas(
            DatosAcademico estudiante,
            int electivasAAsignar,
            Map<Long, Map<Long, AtomicInteger>> cuposRestantesPorOferta,
            List<RespuestaOpcion> opcionesDelEstudiante,
            List<AsignacionElectiva> asignacionesDelEstudiante
    ) {


        int asignacionesRealizadas = 0;

        // 2. Recorrer del 1 al 7 (o las que existan)
        for (int i = 0; i < opcionesDelEstudiante.size(); i++) {

            RespuestaOpcion opcion = opcionesDelEstudiante.get(i);

            // Si ya se asignaron las necesarias → marcar restantes como NO_EVALUADA
            if (asignacionesRealizadas >= electivasAAsignar) {
                return asignacionesRealizadas; // devolver lo asignado
            }

            // Si no seleccionó oferta en esta opción, saltar
            Oferta oferta = opcion.getOferta();
            if (oferta == null) {
                continue;
            }

            // Intentar asignar directamente
            ResultadoAsignacion resultado =
                    intentarAsignarDirectamenteElectiva(
                            estudiante,
                            oferta,
                            opcion.getOpcionNum(),
                            cuposRestantesPorOferta,
                            asignacionesDelEstudiante
                    );

            // Contar solo las asignaciones directas
            if (resultado == ResultadoAsignacion.ASIGNADA) {
                asignacionesRealizadas++;
            }
        }

        // Si llegó al final sin completar → no hay más opciones
        return asignacionesRealizadas;
    }

    /**
     * Intenta asignar una electiva directamente para un estudiante en una oferta específica.
     * Maneja casos de cupos agotados o programa incompatible y actualiza el estado de la asignación.
     *
     * @param estudiante Datos académicos del estudiante.
     * @param oferta Oferta de electiva a asignar.
     * @param numeroOpcion Número de opción del estudiante.
     * @param cuposRestantesPorOferta Map de cupos disponibles por oferta y programa.
     * @param asignacionesDelEstudiante Lista de asignaciones a actualizar.
     * @return ResultadoAsignacion indicando si fue asignada, SIN_CUPO o PROGRAMA_INCOMPATIBLE.
     */
    private ResultadoAsignacion intentarAsignarDirectamenteElectiva(DatosAcademico estudiante, Oferta oferta, int numeroOpcion,  Map<Long, Map<Long, AtomicInteger>> cuposRestantesPorOferta, List<AsignacionElectiva> asignacionesDelEstudiante) {
// 0. Validar duplicados: si la oferta ya tiene un estado distinto de NO_EVALUADA
        boolean yaIntentada = asignacionesDelEstudiante.stream()
                .anyMatch(a -> a.getOferta() != null
                        && a.getOferta().getId().equals(oferta.getId())
                        && a.getEstadoAsignacion() != EstadoAsignacion.NO_EVALUADA);
        if (yaIntentada) {
            actualizarAsignacion(asignacionesDelEstudiante, numeroOpcion, EstadoAsignacion.OPCION_DUPLICADA);
            return ResultadoAsignacion.OPCION_DUPLICADA; // nuevo valor agregado a enum ResultadoAsignacion
        }

        Long ofertaId = oferta.getId();
        Long programaId = estudiante.getRespuesta().getPrograma().getId();


        // 1. Cupos disponibles en memoria
        Map<Long, AtomicInteger> cuposPorPrograma = cuposRestantesPorOferta.get(ofertaId);
        if (cuposPorPrograma == null) {
            actualizarAsignacion(asignacionesDelEstudiante, numeroOpcion, EstadoAsignacion.PROGRAMA_INCOMPATIBLE);;
            return ResultadoAsignacion.PROGRAMA_INCOMPATIBLE;
        }


        // 2. Cupos ya usados
        AtomicInteger cuposRestantes = cuposPorPrograma.get(programaId);

        // 2. Programa NO tiene cupos
        if (cuposRestantes == null) {
            actualizarAsignacion(asignacionesDelEstudiante, numeroOpcion, EstadoAsignacion.PROGRAMA_INCOMPATIBLE);
            return ResultadoAsignacion.PROGRAMA_INCOMPATIBLE;
        }

        // 3. Si NO hay cupos → registrar SIN_CUPO
        if (cuposRestantes.get() <= 0) {
            actualizarAsignacion(asignacionesDelEstudiante, numeroOpcion, EstadoAsignacion.SIN_CUPO);
            return ResultadoAsignacion.SIN_CUPO;
        }

        // 4. Intentar descontar cupo
        int nuevoValor = cuposRestantes.decrementAndGet();
        if (nuevoValor < 0) {
            // rollback del decremento
            cuposRestantes.incrementAndGet();

            actualizarAsignacion(asignacionesDelEstudiante, numeroOpcion, EstadoAsignacion.SIN_CUPO);
            return ResultadoAsignacion.SIN_CUPO;
        }
        //Registrar que se intento pero no hubo cupo
        // 5. Éxito → registrar asignación
        actualizarAsignacion(asignacionesDelEstudiante, numeroOpcion, EstadoAsignacion.ASIGNADA);
        return ResultadoAsignacion.ASIGNADA;
    }

    /**
     * Segundo recorrido del algoritmo: intenta mover a lista de espera aquellas opciones
     * que no pudieron ser asignadas directamente por falta de cupo.
     * Actualiza el estado de las asignaciones y mantiene un límite máximo de estudiantes en lista de espera.
     *
     * @param electivasFaltantes Cantidad de electivas faltantes por asignar.
     * @param asignaciones Lista de asignaciones del estudiante a actualizar.
     * @param listaEsperaPorOferta Contadores de estudiantes en lista de espera por oferta.
     */
    private void procesarListasDeEspera(
            int electivasFaltantes,
            List<AsignacionElectiva> asignaciones,
            Map<Long, AtomicInteger> listaEsperaPorOferta
    ) {

        int asignadasListaEspera = 0;

        for (AsignacionElectiva asignacion : asignaciones)  {

            // Detener si ya se cubrieron las electivas faltantes
            if (asignadasListaEspera >= electivasFaltantes) {
                break;
            }

            // Solo procesar las que quedaron SIN_CUPO
            if (asignacion.getEstadoAsignacion() != EstadoAsignacion.SIN_CUPO) {
                continue;
            }

            Long ofertaId = asignacion.getOferta().getId();

            // <-- Validar cupos en memoria
            AtomicInteger contador = listaEsperaPorOferta.get(ofertaId);

            if (contador.get() >= 7) {
                asignacion.setEstadoAsignacion(EstadoAsignacion.SIN_CUPO_LISTA_ESPERA);
                asignacion.setFechaAsignacion(Instant.now());
                continue;
            }

            // <-- Mover a lista de espera
            asignacion.setEstadoAsignacion(EstadoAsignacion.LISTA_ESPERA);
            asignacion.setFechaAsignacion(Instant.now());
            contador.incrementAndGet();

            asignadasListaEspera++;
        }

    }



    /**
     * Actualiza el estado de una asignación específica dentro de la lista de asignaciones del estudiante.
     * Registra la fecha de la actualización.
     *
     * @param asignaciones Lista de asignaciones del estudiante.
     * @param numeroOpcion Número de opción de la asignación a actualizar.
     * @param estado Nuevo estado de la asignación.
     */
    private void actualizarAsignacion(
            List<AsignacionElectiva> asignaciones,
            int numeroOpcion,
            EstadoAsignacion estado
    ) {
        asignaciones.stream()
                .filter(a -> a.getNumeroOpcion() == numeroOpcion)
                .findFirst()
                .ifPresent(a -> {
                    a.setEstadoAsignacion(estado);
                    a.setFechaAsignacion(Instant.now());
                });
    }

}