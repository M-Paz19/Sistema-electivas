package com.unicauca.fiet.sistema_electivas.reporte.service;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteAsignacionReporteResponse;
import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import com.unicauca.fiet.sistema_electivas.asignacion.model.AsignacionElectiva;
import com.unicauca.fiet.sistema_electivas.asignacion.repository.AsignacionElectivaRepository;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ProgramaElectivaRepository;
import com.unicauca.fiet.sistema_electivas.integracion.python.MotorPythonClient;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoRespuestaFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.DatosAcademicoMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.RespuestaFormularioMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.repository.DatosAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import com.unicauca.fiet.sistema_electivas.programa.repository.ProgramaRepository;
import com.unicauca.fiet.sistema_electivas.programa.util.ProgramaSiglaUtil;
import com.unicauca.fiet.sistema_electivas.reporte.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportesEstadisticasServiceImpl implements ReportesEstadisticasService {
    private final PeriodoAcademicoRepository periodoAcademicoRepository;
    private final AsignacionElectivaRepository asignacionElectivaRepository;
    private final DatosAcademicoRepository datosAcademicoRepository;
    private  final RespuestasFormularioRepository respuestasFormularioRepository;
    private  final ProgramaElectivaRepository programaElectivaRepository;
    private final MotorPythonClient motorPythonClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public DistribucionAsignacionesResponse obtenerDistribucionAsignaciones(Long periodoId) {

        // 1. Validar existencia del período
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        // 2. Validar estado del periodo (solo cuando ya se terminó la asignación)
        if (periodo.getEstado() != EstadoPeriodoAcademico.ASIGNACION_PROCESADA && periodo.getEstado() != EstadoPeriodoAcademico.CERRADO) {
            throw new InvalidStateException(
                    "Las estadísticas solo pueden consultarse cuando el período está en estado ASIGNACION_PROCESADA o CERRADO."
            );
        }

        // 3. Traer asignaciones del período
        List<AsignacionElectiva> asignaciones =
                asignacionElectivaRepository.findByPeriodoId(periodoId);

        // 4. Convertir a DTO minimal
        List<AsignacionElectivaMinDto> dtoList = asignaciones.stream()
                .map(a -> new AsignacionElectivaMinDto(
                        a.getEstudianteCodigo(),
                        a.getNumeroOpcion(),
                        a.getEstadoAsignacion()
                ))
                .toList();

        // 5. Llamar al microservicio Python
        DistribucionAsignacionesResponse response = motorPythonClient.calcularDistribucion(dtoList);

        // 6. Ajustar el periodoId si Python lo deja en 0
        response.setSemestre(periodo.getSemestre());

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DistribucionAsignacionesPorProgramaResponse obtenerDistribucionPorPrograma(Long periodoId) {

        // 1. Validar existencia del período
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        // 2. Validar estado
        if (periodo.getEstado() != EstadoPeriodoAcademico.ASIGNACION_PROCESADA &&
                periodo.getEstado() != EstadoPeriodoAcademico.CERRADO) {

            throw new InvalidStateException(
                    "Las estadísticas por programa solo pueden consultarse cuando el período está en estado ASIGNACION_PROCESADA o CERRADO."
            );
        }

        // 3. Obtener asignaciones
        List<AsignacionElectiva> asignaciones =
                asignacionElectivaRepository.findByPeriodoId(periodoId);

        // 3. Obtener estudiantes aptos
        List<DatosAcademico> aptos = datosAcademicoRepository
                .findByRespuesta_PeriodoIdAndEstadoAptitud(
                        periodoId,
                        EstadoAptitud.ASIGNACION_PROCESADA
                );

        // Crear mapa código -> programa
        Map<String, String> mapaProgramas = aptos.stream()
                .collect(Collectors.toMap(
                        DatosAcademico::getCodigoEstudiante,
                        DatosAcademico::getPrograma
                ));

        // 4. Convertir a DTO para el microservicio Python
        List<AsignacionElectivaProgramaDto> dtoList = asignaciones.stream()
                .map(a -> new AsignacionElectivaProgramaDto(
                        a.getEstudianteCodigo(),
                        a.getNumeroOpcion(),
                        a.getEstadoAsignacion(),
                        mapaProgramas.getOrDefault(a.getEstudianteCodigo(), "DESCONOCIDO")  // <— NUEVO CAMPO
                ))
                .toList();

        // 5. Llamar al microservicio Python
        DistribucionAsignacionesPorProgramaResponse response =
                motorPythonClient.calcularDistribucionPorPrograma(dtoList, periodo.getSemestre());

        response.setSemestre(periodo.getSemestre());
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResumenProcesamientoPeriodoResponse obtenerResumenProcesamiento(Long periodoId) {

        // 1. Validar existencia del período
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        // 2. Validar estado
        if (periodo.getEstado() != EstadoPeriodoAcademico.ASIGNACION_PROCESADA &&
                periodo.getEstado() != EstadoPeriodoAcademico.CERRADO) {

            throw new InvalidStateException(
                    "Las estadísticas por programa solo pueden consultarse cuando el período está en estado ASIGNACION_PROCESADA o CERRADO."
            );
        }

        // 3. Respuestas del periodo
        List<RespuestasFormulario> respuestas =
                respuestasFormularioRepository.findByPeriodoId(periodoId);

        // 4. Datos académicos del periodo
        List<DatosAcademico> datos =
                datosAcademicoRepository.findByRespuesta_PeriodoId(periodoId);

        // 3. Agrupación de estados del formulario
        Map<EstadoRespuestaFormulario, Long> resumenFormulario =
                respuestas.stream()
                        .collect(Collectors.groupingBy(
                                RespuestasFormulario::getEstado,
                                Collectors.counting()
                        ));

        // 4. Agrupación de estados de aptitud
        Map<EstadoAptitud, Long> resumenAptitud =
                datos.stream()
                        .collect(Collectors.groupingBy(
                                DatosAcademico::getEstadoAptitud,
                                Collectors.counting()
                        ));

        // 5. Convertir todo en un único listado
        List<ResumenEstadoItem> itemsUnificados = new ArrayList<>();

        // Estados permitidos para FORMULARIO en el orden deseado
        List<EstadoRespuestaFormulario> estadosFormularioOrdenados = List.of(
                EstadoRespuestaFormulario.DUPLICADO,
                EstadoRespuestaFormulario.NO_CUMPLE,
                EstadoRespuestaFormulario.DESCARTADO,
                EstadoRespuestaFormulario.DESCARTADO_SIMCA
        );

        // Estados permitidos para APTITUD en el orden deseado
        List<EstadoAptitud> estadosAptitudOrdenados = List.of(
                EstadoAptitud.NO_APTO,
                EstadoAptitud.EXCLUIDO_POR_ELECTIVAS,
                EstadoAptitud.ASIGNACION_PROCESADA
        );
        // ------------------------------
        // FORMULARIO
        // ------------------------------
        for (EstadoRespuestaFormulario estado : estadosFormularioOrdenados) {
            Long cantidad = resumenFormulario.getOrDefault(estado, 0L);
            itemsUnificados.add(
                    new ResumenEstadoItem(
                            "FORMULARIO",
                            estado.name(),
                            estado.getDescripcion(),
                            cantidad
                    )
            );
        }
        // ------------------------------
        // APTITUD
        // ------------------------------
        for (EstadoAptitud estado : estadosAptitudOrdenados) {
            Long cantidad = resumenAptitud.getOrDefault(estado, 0L);
            itemsUnificados.add(
                    new ResumenEstadoItem(
                            "APTITUD",
                            estado.name(),
                            estado.getDescripcion(),
                            cantidad
                    )
            );
        }

        // 6. Respuesta final
        return new ResumenProcesamientoPeriodoResponse(
                periodoId,
                (long) respuestas.size(),
                itemsUnificados
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generarReporteDistribucionExcel(Long periodoId){
        // 1. Validar estado del periodo (ya lo hacen los métodos internos)
        DistribucionAsignacionesResponse distribucionEstudiantes =obtenerDistribucionAsignaciones(periodoId);

        DistribucionAsignacionesPorProgramaResponse distribucionProgramas =obtenerDistribucionPorPrograma(periodoId);

        ResumenProcesamientoPeriodoResponse resumenPeriodo = obtenerResumenProcesamiento(periodoId);

        // 2. Llamar al microservicio Python para generar Excel
        return motorPythonClient.generarReporteDistribucionExcel(
                distribucionEstudiantes,
                distribucionProgramas,
                resumenPeriodo
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PopularidadElectivasResponse obtenerPopularidad(Long periodoId) {

        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        if (periodo.getEstado() != EstadoPeriodoAcademico.ASIGNACION_PROCESADA &&
                periodo.getEstado() != EstadoPeriodoAcademico.CERRADO) {
            throw new InvalidStateException("No se puede consultar el reporte aún.");
        }

        // 1. Obtener directamente las últimas respuestas válidas (sin duplicados y solo aptos)
        List<RespuestasFormulario> respuestas = datosAcademicoRepository
                .findValidosConOpcionesPorPeriodo(periodoId)
                .stream()
                .map(DatosAcademico::getRespuesta)
                .distinct()
                .toList();

        if (respuestas.isEmpty()) {
            return new PopularidadElectivasResponse(); // vacío
        }

        // 2. Convertir a DTO para Python
        // 2. Convertir a DTO para Python
        List<PopularidadRequestDto> dto = respuestas.stream()
                .flatMap(r -> r.getOpciones().stream()
                        // Filtrar opciones sin oferta (importante)
                        .filter(op -> op.getOferta() != null && op.getOferta().getElectiva() != null)
                        .map(op -> new PopularidadRequestDto(
                                r.getCodigoEstudiante(),
                                op.getOpcionNum(),
                                op.getOferta().getElectiva().getNombre()
                                        + ProgramaSiglaUtil.generarSiglasProgramas(
                                        obtenerNombresProgramas(op.getOferta().getElectiva().getId())
                                )
                        ))
                )
                .toList();

        // 3. Procesar en Python
        PopularidadElectivasResponse  response = motorPythonClient.calcularPopularidad(dto);
        response.setSemestre(periodo.getSemestre());
        return response;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PopularidadElectivasResponse obtenerPopularidadIncluyendoDescartados(Long periodoId) {

        PeriodoAcademico periodo = periodoAcademicoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));

        if (periodo.getEstado() != EstadoPeriodoAcademico.ASIGNACION_PROCESADA &&
                periodo.getEstado() != EstadoPeriodoAcademico.CERRADO) {
            throw new InvalidStateException("No se puede consultar el reporte aún.");
        }

        // Estados permitidos
        List<EstadoRespuestaFormulario> estadosPermitidos = List.of(
                EstadoRespuestaFormulario.NO_CUMPLE,
                EstadoRespuestaFormulario.DESCARTADO,
                EstadoRespuestaFormulario.DESCARTADO_SIMCA,
                EstadoRespuestaFormulario.DATOS_CARGADOS
        );

        // 1. Recuperar respuestas del período filtradas por estado
        List<RespuestasFormulario> respuestas = respuestasFormularioRepository
                .findByPeriodoAndEstadosWithOpciones(periodoId, estadosPermitidos)
                .stream()
                .distinct() // proteger en caso de joins accidentales
                .toList();

        if (respuestas.isEmpty()) {
            return new PopularidadElectivasResponse();
        }

        // 2. Convertir a DTO para Python
        List<PopularidadRequestDto> dto = respuestas.stream()
                .flatMap(r -> r.getOpciones().stream()
                        .filter(op -> op.getOferta() != null && op.getOferta().getElectiva() != null)
                        .map(op -> new PopularidadRequestDto(
                                r.getCodigoEstudiante(),
                                op.getOpcionNum(),
                                op.getOferta().getElectiva().getNombre()
                                        + ProgramaSiglaUtil.generarSiglasProgramas(
                                        obtenerNombresProgramas(op.getOferta().getElectiva().getId())
                                )
                        ))
                )
                .toList();

        // 3. Procesar en Python
        PopularidadElectivasResponse response = motorPythonClient.calcularPopularidad(dto);
        response.setSemestre(periodo.getSemestre());
        return response;
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
     * {@inheritDoc}
     */
    @Override
    public byte[] generarReportePopularidadExcel(Long periodoId) {

        // 1. Obtener popularidad normal (solo válidos/aptos)
        PopularidadElectivasResponse popularidadAptos =
                obtenerPopularidad(periodoId);

        // 2. Obtener popularidad incluyendo descartados
        PopularidadElectivasResponse popularidadIncluyendoDescartados =
                obtenerPopularidadIncluyendoDescartados(periodoId);

        // 3. Llamar al microservicio Python para generar el Excel
        return motorPythonClient.generarReportePopularidadExcel(
                popularidadAptos,
                popularidadIncluyendoDescartados
        );
    }



}
