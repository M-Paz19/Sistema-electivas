package com.unicauca.fiet.sistema_electivas.asignacion.service;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.*;
import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import com.unicauca.fiet.sistema_electivas.asignacion.mapper.AsignacionMapper;
import com.unicauca.fiet.sistema_electivas.asignacion.mapper.OrdenamientoMapper;
import com.unicauca.fiet.sistema_electivas.asignacion.mapper.ReporteAsignacionMapper;
import com.unicauca.fiet.sistema_electivas.asignacion.model.AsignacionElectiva;
import com.unicauca.fiet.sistema_electivas.asignacion.repository.AsignacionElectivaRepository;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ProgramaElectivaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.OfertaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.repository.DatosAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultaAsignacionServiceImpl implements ConsultaAsignacionService {
    @Autowired
    private PeriodoAcademicoRepository periodoAcademicoRepository;
    @Autowired
    private AsignacionElectivaRepository asignacionElectivaRepository;
    @Autowired
    private OrdenamientoMapper ordenamientoMapper; // para convertir entidades a DTOs
    @Autowired
    private AsignacionMapper asignacionMapper;
    @Autowired
    private OfertaRepository ofertaRepository;
    @Autowired
    private ProgramaElectivaRepository programaElectivaRepository;
    @Autowired
    private DatosAcademicoRepository datosAcademicoRepository;

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public List<EstudianteOrdenamientoResponse> obtenerAptosOrdenados(Long periodoId) {
        // 1. Buscar período
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período académico no encontrado."));

        // 2. Validar estado PROCESO_FILTRADO_NO_ELEGIBLES
        if (periodo.getEstado() != EstadoPeriodoAcademico.EN_PROCESO_ASIGNACION) {
            throw new InvalidStateException(
                    "Solo se puede obtener el listado de estudiantes aptos ordenados cuando el período está en estado EN_PROCESO_ASIGNACION."
            );
        }
        // Trae solo los APTO
        List<DatosAcademico> aptos =
                datosAcademicoRepository.findByRespuesta_PeriodoIdAndEstadoAptitud(
                        periodoId,
                        EstadoAptitud.APTO
                );

        // Ordenamiento por criterios oficiales
        aptos.sort(Comparator
                // 1 porcentaje avance DESC
                .comparing(DatosAcademico::getPorcentajeAvance, Comparator.reverseOrder())
                // 2 promedio carrera DESC
                .thenComparing(DatosAcademico::getPromedioCarrera, Comparator.reverseOrder())
                // 3 electivas faltantes ASC ("faltan menos" → más prioridad)
                .thenComparing(d -> d.getPlanEstudios().getElectivasRequeridas() - d.getAprobadas())
        );

        return ordenamientoMapper.toResponseList(aptos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DatosAcademico> obtenerAptosOrdenadosInterno(Long periodoId) {
        // 1. Buscar período
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período académico no encontrado."));

        // 2. Validar estado PROCESO_FILTRADO_NO_ELEGIBLES
        if (periodo.getEstado() != EstadoPeriodoAcademico.EN_PROCESO_ASIGNACION) {
            throw new InvalidStateException(
                    "Solo se puede obtener el listado de estudiantes aptos ordenados cuando el período está en estado EN_PROCESO_ASIGNACION."
            );
        }
        // Consultar todos los estudiantes APTO del período
        List<DatosAcademico> aptos =
                datosAcademicoRepository.findAptosConPlanByPeriodo(
                        periodoId,
                        EstadoAptitud.APTO
                );

        // Aplicar el ordenamiento oficial
        aptos.sort(Comparator
                // 1. Porcentaje de avance (DESC)
                .comparing(DatosAcademico::getPorcentajeAvance, Comparator.reverseOrder())
                // 2. Promedio carrera (DESC)
                .thenComparing(DatosAcademico::getPromedioCarrera, Comparator.reverseOrder())
                // 3. Electivas faltantes (ASC)
                .thenComparing(d -> d.getPlanEstudios().getElectivasRequeridas() - d.getAprobadas())
        );

        return aptos;
    }
    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public List<DepartamentoReporteDTO> generarListasDeAsigancionPorDepartamentos(Long periodoId) {
        // 1. Buscar período
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período académico no encontrado."));

        // 2. Validar estado PROCESO_FILTRADO_NO_ELEGIBLES
        if (periodo.getEstado() != EstadoPeriodoAcademico.ASIGNACION_PROCESADA) {
            throw new InvalidStateException(
                    "Solo se puede generar el reporte de listas de asignación cuando el período está en estado ASIGNACION_PROCESADA."
            );
        }
        // 2. Obtener ofertas agrupadas por departamento
        Map<Departamento, List<Oferta>> ofertasPorDepartamento = obtenerOfertasAgrupadas(periodoId);
        // 3. Construir respuesta
        List<DepartamentoReporteDTO> respuesta = new ArrayList<>();

        for (Map.Entry<Departamento, List<Oferta>> entry : ofertasPorDepartamento.entrySet()) {

            Departamento departamento = entry.getKey();
            List<Oferta> ofertasDepto = entry.getValue();

            DepartamentoReporteDTO dto = ReporteAsignacionMapper.toDepartamentoDTO(departamento);
            // Convertir cada oferta
            List<OfertaReporteDTO> ofertasDTO = ofertasDepto.stream()
                    .map(o -> {

                        List<String> programas = obtenerNombresProgramas(o.getElectiva().getId());
                        List<EstudianteAsignacionDTO> estudiantes = obtenerEstudiantesOrdenados(o);

                        return ReporteAsignacionMapper.toOfertaDTO(o, programas, estudiantes);
                    })
                    .collect(Collectors.toList());

            dto.setOfertas(ofertasDTO);
            respuesta.add(dto);
        }
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public List<EstudianteAsignacionReporteResponse> generarReporteRanking(Long periodoId) {
        // 1. Buscar período
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período académico no encontrado."));

        // 2. Validar estado PROCESO_FILTRADO_NO_ELEGIBLES
        if (periodo.getEstado() != EstadoPeriodoAcademico.ASIGNACION_PROCESADA) {
            throw new InvalidStateException(
                    "Solo se puede generar el reporte de ranking cuando el período está en estado ASIGNACION_PROCESADA."
            );
        }
        // 3. Obtener estudiantes aptos
        List<DatosAcademico> aptos = datosAcademicoRepository.findByRespuesta_PeriodoIdAndEstadoAptitud(
                periodoId,
                EstadoAptitud.ASIGNACION_PROCESADA
        );

        // 4. Ordenamiento oficial
        aptos.sort(Comparator
                .comparing(DatosAcademico::getPorcentajeAvance, Comparator.reverseOrder())
                .thenComparing(DatosAcademico::getPromedioCarrera, Comparator.reverseOrder())
                .thenComparing(d -> d.getPlanEstudios().getElectivasRequeridas() - d.getAprobadas())
        );

        // 5. Construir reporte completo
        List<EstudianteAsignacionReporteResponse> reporte = new ArrayList<>();

        for (DatosAcademico d : aptos) {

            // Obtener asignaciones de este estudiante
            List<AsignacionElectiva> asignaciones = asignacionElectivaRepository.findByEstudianteAndPeriodo(
                    d.getCodigoEstudiante(),
                    periodoId
            );

            // Construir mapa de programas por electiva para este estudiante
            Map<Long, List<String>> programasPorElectiva = asignaciones.stream()
                    .map(a -> a.getOferta().getElectiva())
                    .distinct()
                    .collect(Collectors.toMap(
                            e -> e.getId(),
                            e -> obtenerNombresProgramas(e.getId()) // método que trae nombres de programas de la electiva
                    ));

            // Usar mapper para construir DTO
            EstudianteAsignacionReporteResponse dto = asignacionMapper.toReporte(d, asignaciones, programasPorElectiva);

            reporte.add(dto);
        }

        return reporte;
    }

    /**
     * Obtiene todas las ofertas de un período y las agrupa por departamento,
     * ordenándolas previamente por nombre de la electiva.
     *
     * <p>El resultado es un mapa donde la llave es el departamento y el valor
     * es la lista de ofertas pertenecientes a ese departamento.</p>
     *
     * @param periodoId ID del período académico
     * @return mapa Departamento → Lista de ofertas asociadas
     */
    private Map<Departamento, List<Oferta>> obtenerOfertasAgrupadas(Long periodoId) {

        List<Oferta> ofertas = ofertaRepository.findByPeriodoId(periodoId);

        return ofertas.stream()
                .sorted(Comparator.comparing(o -> o.getElectiva().getNombre()))
                .collect(Collectors.groupingBy(o -> o.getElectiva().getDepartamento()));
    }

    /**
     * Obtiene los nombres de todos los programas que tienen asociado
     * un vínculo con la electiva especificada.
     *
     * <p>Este método se utiliza para construir el reporte de ofertas,
     * donde se requiere mostrar los programas que pueden cursar la electiva.</p>
     *
     * @param electivaId ID de la electiva
     * @return lista de nombres de programas relacionados
     */
    private List<String> obtenerNombresProgramas(Long electivaId) {

        return programaElectivaRepository.findProgramasByElectivaId(electivaId)
                .stream()
                .map(Programa::getNombre)
                .collect(Collectors.toList());
    }

    /**
     * Genera un string con las siglas de los programas asociados a una electiva.
     *
     * <p>Por ejemplo, si los programas son ["Ingeniería de Sistemas", "Licenciatura en Matemáticas"],
     * el resultado sería: [pIS-pLM].</p>
     *
     * @param electivaId ID de la electiva
     * @return String con siglas de los programas entre corchetes
     */
    private String generarSiglasProgramas(Long electivaId) {
        List<String> nombresProgramas = obtenerNombresProgramas(electivaId);

        List<String> siglas = nombresProgramas.stream()
                .map(nombre -> {
                    String[] palabras = nombre.split("\\s+");
                    StringBuilder sb = new StringBuilder("p"); // prefijo 'p'
                    for (String palabra : palabras) {
                        palabra = palabra.toLowerCase();
                        // Ignorar palabras comunes
                        if (!List.of("de", "y", "la", "el", "en", "del").contains(palabra)) {
                            sb.append(Character.toUpperCase(palabra.charAt(0)));
                        }
                    }
                    return sb.toString();
                })
                .collect(Collectors.toList());

        return "[" + String.join("-", siglas) + "]";
    }

    /**
     * Obtiene la lista de estudiantes de una oferta (asignados y en lista de espera)
     * y los ordena aplicando los criterios oficiales:
     * <ol>
     *   <li>Porcentaje de avance (DESC)</li>
     *   <li>Promedio de carrera (DESC)</li>
     *   <li>Electivas faltantes (ASC)</li>
     * </ol>
     *
     * <p>También convierte el resultado a DTOs usando el mapper correspondiente
     * y asigna numeración consecutiva para ser mostrada en el reporte.</p>
     *
     * <p>Los estudiantes sin datos académicos válidos no se incluyen en el resultado.</p>
     *
     * @param oferta oferta de la electiva
     * @return lista ordenada de estudiantes en formato DTO
     */
    private List<EstudianteAsignacionDTO> obtenerEstudiantesOrdenados(Oferta oferta) {

        // 1. Obtener todas las asignaciones de la oferta
        List<AsignacionElectiva> asignaciones =
                asignacionElectivaRepository.findByOfertaIdAndEstadoAsignacionIn(
                        oferta.getId(),
                        List.of(EstadoAsignacion.ASIGNADA, EstadoAsignacion.LISTA_ESPERA)
                );

        // 2. Cargar datos académicos válidos
        List<DatosAcademico> datos = asignaciones.stream()
                .map(a -> datosAcademicoRepository
                        .findByCodigoAndPeriodo(a.getEstudianteCodigo(), oferta.getPeriodo().getId())
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 3. Separar en asignados y lista de espera
        List<DatosAcademico> asignados = new ArrayList<>();
        List<DatosAcademico> listaEspera = new ArrayList<>();

        for (DatosAcademico d : datos) {
            AsignacionElectiva a = asignaciones.stream()
                    .filter(x -> x.getEstudianteCodigo().equals(d.getCodigoEstudiante()))
                    .findFirst()
                    .orElse(null);

            if (a != null && a.getEstadoAsignacion() == EstadoAsignacion.ASIGNADA) {
                asignados.add(d);
            } else if (a != null && a.getEstadoAsignacion() == EstadoAsignacion.LISTA_ESPERA) {
                listaEspera.add(d);
            }
        }

        // 4. Ordenar cada grupo por criterios oficiales
        ordenarDatosAcademicos(asignados);
        ordenarDatosAcademicos(listaEspera);

        // 5. Concatenar listas: primero asignados, luego lista de espera
        List<DatosAcademico> datosOrdenados = new ArrayList<>();
        datosOrdenados.addAll(asignados);
        datosOrdenados.addAll(listaEspera);

        // 6. Convertir a DTO con numeración
        AtomicInteger contador = new AtomicInteger(1);

        return datosOrdenados.stream()
                .map(d -> {
                    AsignacionElectiva a = asignaciones.stream()
                            .filter(x -> x.getEstudianteCodigo().equals(d.getCodigoEstudiante()))
                            .findFirst()
                            .orElse(null);

                    return ReporteAsignacionMapper.toEstudianteDTO(
                            d,
                            a.getEstadoAsignacion(),
                            contador.getAndIncrement()
                    );
                })
                .collect(Collectors.toList());
    }


    /**
     * Aplica el ordenamiento oficial sobre la lista de datos académicos.
     *
     * <p>Los criterios se aplican en el siguiente orden:</p>
     * <ol>
     *   <li>Porcentaje de avance (DESC)</li>
     *   <li>Promedio de carrera (DESC)</li>
     *   <li>Electivas faltantes (ASC)</li>
     * </ol>
     *
     * <p>Este método modifica directamente la lista recibida.</p>
     *
     * @param datos lista de entidades DatosAcademico a ordenar
     */
    private void ordenarDatosAcademicos(List<DatosAcademico> datos) {

        datos.sort(Comparator
                .comparing(DatosAcademico::getPorcentajeAvance, Comparator.reverseOrder())
                .thenComparing(DatosAcademico::getPromedioCarrera, Comparator.reverseOrder())
                .thenComparing(d -> d.getPlanEstudios().getElectivasRequeridas() - d.getAprobadas())
        );
    }
}
