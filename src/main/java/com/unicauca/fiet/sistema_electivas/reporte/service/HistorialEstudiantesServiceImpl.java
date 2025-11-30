package com.unicauca.fiet.sistema_electivas.reporte.service;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteAsignacionReporteResponse;
import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import com.unicauca.fiet.sistema_electivas.asignacion.model.AsignacionElectiva;
import com.unicauca.fiet.sistema_electivas.asignacion.repository.AsignacionElectivaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.DatosAcademicoMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.RespuestaFormularioMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.repository.DatosAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.reporte.dto.EstudianteBusquedaResponse;
import com.unicauca.fiet.sistema_electivas.reporte.dto.HistorialEstudiantePeriodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class HistorialEstudiantesServiceImpl implements HistorialEstudiantesService {
    private final AsignacionElectivaRepository asignacionElectivaRepository;
    private final DatosAcademicoRepository datosAcademicoRepository;
    private  final RespuestasFormularioRepository respuestasFormularioRepository;
    private final DatosAcademicoMapper datosAcademicoMapper;
    /**
     * {@inheritDoc}
     */
    @Override
    public List<HistorialEstudiantePeriodoResponse> obtenerHistorialPorEstudiante(String codigoEstudiante) {

        List<DatosAcademico> datos = datosAcademicoRepository.findHistorialDatosAcademicos(codigoEstudiante);
        List<RespuestasFormulario> respuestas = respuestasFormularioRepository.findHistorialRespuestas(codigoEstudiante);
        List<AsignacionElectiva> asignaciones = asignacionElectivaRepository.findHistorialAsignaciones(codigoEstudiante);

        // Agrupación por periodo (clave = "2024-1", etc.)
        Map<String, HistorialEstudiantePeriodoResponse> historial = new LinkedHashMap<>();

        // ============ Datos Académicos ============
        for (DatosAcademico da : datos) {
            String periodo = da.getArchivoCargado().getPeriodo().getSemestre(); // ej: "2025-1"

            historial.putIfAbsent(periodo, new HistorialEstudiantePeriodoResponse());
            var h = historial.get(periodo);

            h.setPeriodo(periodo);
            h.setDatosAcademicos(datosAcademicoMapper.toResponse(da));
        }

        // ============ Respuestas de Formulario ============
        for (RespuestasFormulario rf : respuestas) {
            String periodo = rf.getPeriodo().getSemestre();

            historial.putIfAbsent(periodo, new HistorialEstudiantePeriodoResponse());
            var h = historial.get(periodo);

            if (h.getRespuestas() == null)
                h.setRespuestas(new ArrayList<>());

            h.getRespuestas().add(RespuestaFormularioMapper.toResponse(rf));
        }

        // ============ Asignaciones ============
        for (AsignacionElectiva ae : asignaciones) {

            String periodo = ae.getOferta().getPeriodo().getSemestre();

            historial.putIfAbsent(periodo, new HistorialEstudiantePeriodoResponse());
            var h = historial.get(periodo);

            if (h.getAsignaciones() == null)
                h.setAsignaciones(new ArrayList<>());

            var info = new EstudianteAsignacionReporteResponse.AsignacionElectivaInfo();
            info.setNumeroOpcion(ae.getNumeroOpcion());
            info.setEstado(ae.getEstadoAsignacion());
            info.setNombreElectiva(ae.getOferta().getElectiva().getNombre());

            h.getAsignaciones().add(info);

            // === CONTADORES POR PERÍODO ===
            if (ae.getEstadoAsignacion() == EstadoAsignacion.ASIGNADA) {
                h.setTotalAsignadas(h.getTotalAsignadas() + 1);
            }
            if (ae.getEstadoAsignacion() == EstadoAsignacion.LISTA_ESPERA) {
                h.setTotalListaEspera(h.getTotalListaEspera() + 1);
            }
        }

        return new ArrayList<>(historial.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EstudianteBusquedaResponse> buscar(String filtro) {

        // 1. Buscar en datos académicos
        List<EstudianteBusquedaResponse> desdeDatos = datosAcademicoRepository.buscarCoincidencias(filtro)
                .stream()
                .map(this::mapDesdeDatos)
                .toList();

        // 2. Buscar en respuestas de formulario
        List<EstudianteBusquedaResponse> desdeRespuestas = respuestasFormularioRepository.buscarCoincidencias(filtro)
                .stream()
                .map(this::mapDesdeRespuestas)
                .toList();

        // 3. Combinar ambas listas y eliminar duplicados por codigoEstudiante
        return Stream.concat(desdeDatos.stream(), desdeRespuestas.stream())
                .collect(Collectors.toMap(
                        EstudianteBusquedaResponse::getCodigoEstudiante, // clave
                        e -> e,                                         // valor
                        (e1, e2) -> e1                                  // si hay duplicado, conservar el primero
                ))
                .values()
                .stream()
                .toList();
    }

    /**
     * Convierte un objeto {@link DatosAcademico} a {@link EstudianteBusquedaResponse}.
     * <p>Se extraen solo los campos mínimos necesarios para la búsqueda:
     * código de estudiante, nombres, apellidos y programa.</p>
     *
     * @param d entidad DatosAcademico
     * @return DTO con información mínima del estudiante
     */
    private EstudianteBusquedaResponse mapDesdeDatos(DatosAcademico d) {
        EstudianteBusquedaResponse r = new EstudianteBusquedaResponse();
        r.setCodigoEstudiante(d.getCodigoEstudiante());
        r.setNombres(d.getNombres());
        r.setApellidos(d.getApellidos());
        r.setPrograma(d.getPrograma());
        return r;
    }


    /**
     * Convierte un objeto {@link RespuestasFormulario} a {@link EstudianteBusquedaResponse}.
     * <p>Se extraen los mismos campos mínimos que en mapDesdeDatos y se obtiene
     * el nombre del programa si existe.</p>
     *
     * @param r0 entidad RespuestasFormulario
     * @return DTO con información mínima del estudiante
     */
    private EstudianteBusquedaResponse mapDesdeRespuestas(RespuestasFormulario r0) {
        EstudianteBusquedaResponse r = new EstudianteBusquedaResponse();
        r.setCodigoEstudiante(r0.getCodigoEstudiante());
        r.setNombres(r0.getNombreEstudiante());
        r.setApellidos(r0.getApellidosEstudiante());
        r.setPrograma(r0.getPrograma() != null ? r0.getPrograma().getNombre() : null);
        return r;
    }
}
