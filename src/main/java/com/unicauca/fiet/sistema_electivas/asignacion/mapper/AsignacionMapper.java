package com.unicauca.fiet.sistema_electivas.asignacion.mapper;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteAsignacionReporteResponse;
import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import com.unicauca.fiet.sistema_electivas.asignacion.model.AsignacionElectiva;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class AsignacionMapper {

    /**
     * Convierte un estudiante y sus asignaciones en un DTO de reporte.
     *
     * @param datos              Datos académicos del estudiante
     * @param asignaciones       Lista de asignaciones del estudiante
     * @param programasPorElectiva   Lista de nombres de programas de cada electiva
     * @return DTO completo para el reporte
     */
    public EstudianteAsignacionReporteResponse toReporte(
            DatosAcademico datos,
            List<AsignacionElectiva> asignaciones,
            Map<Long, List<String>> programasPorElectiva
    ) {
        EstudianteAsignacionReporteResponse dto = new EstudianteAsignacionReporteResponse();

        // -----------------------
        // Datos básicos y métricas
        // -----------------------
        dto.setId(datos.getId());
        dto.setCodigoEstudiante(datos.getCodigoEstudiante());
        dto.setApellidos(datos.getApellidos());
        dto.setNombres(datos.getNombres());
        dto.setUsuario(datos.getUsuario());
        dto.setPrograma(datos.getPrograma());
        dto.setCreditosAprobadosTotal(datos.getCreditosAprobados());
        int creditosTotales = datos.getPlanEstudios().getCreditosTotalesPlan();
        int creditosElectivas = datos.getPlanEstudios().getElectivasRequeridas()*3;
        int creditosTG = datos.getPlanEstudios().getCreditosTrabajoGrado();

        // Ajuste: créditos obligatorios del plan
        int creditosObligatorios = creditosTotales - creditosElectivas - creditosTG;

        // Ajuste: créditos aprobados del estudiante sin electivas
        int creditosEstudianteObligatorios = datos.getCreditosAprobados() - (datos.getAprobadas() * 3);
        dto.setCreditosAprobadosObligatorio(creditosEstudianteObligatorios);
        dto.setCreditosPensumObligatorio(creditosObligatorios);

        dto.setPeriodosMatriculados(datos.getPeriodosMatriculados());
        dto.setEsNivelado(datos.getEsNivelado());
        dto.setPorcentajeAvance(datos.getPorcentajeAvance());
        dto.setPromedioCarrera(datos.getPromedioCarrera());
        dto.setDebeVer(datos.getPlanEstudios().getElectivasRequeridas());
        dto.setAprobadas(datos.getAprobadas());
        dto.setFaltan(datos.getPlanEstudios().getElectivasRequeridas() - datos.getAprobadas());

        // -----------------------
        // Asignaciones
        // -----------------------
        List<EstudianteAsignacionReporteResponse.AsignacionElectivaInfo> asignacionesDTO =
                asignaciones.stream()
                        .map(a -> mapAsignacion(a, programasPorElectiva.get(a.getOferta().getElectiva().getId())))
                        .collect(Collectors.toList());

        dto.setAsignadas((int) asignaciones.stream()
                .filter(a -> a.getEstadoAsignacion() == EstadoAsignacion.ASIGNADA)
                .count());
        dto.setListaDeEspera((int) asignaciones.stream()
                .filter(a -> a.getEstadoAsignacion() == EstadoAsignacion.LISTA_ESPERA)
                .count());
        dto.setAsignaciones(asignacionesDTO);

        return dto;
    }

    /**
     * Convierte una asignación de electiva en el DTO correspondiente.
     *
     * @param asignacion         Asignación de electiva
     * @param nombresProgramas   Lista de nombres de programas de la electiva
     * @return DTO de asignación para reporte
     */
    private EstudianteAsignacionReporteResponse.AsignacionElectivaInfo mapAsignacion(
            AsignacionElectiva asignacion,
            List<String> nombresProgramas
    ) {
        EstudianteAsignacionReporteResponse.AsignacionElectivaInfo info =
                new EstudianteAsignacionReporteResponse.AsignacionElectivaInfo();
        info.setNumeroOpcion(asignacion.getNumeroOpcion());
        info.setNombreElectiva(
                asignacion.getOferta().getElectiva().getNombre() +
                        generarSiglasProgramas(nombresProgramas)
        );
        info.setEstado(asignacion.getEstadoAsignacion());

        return info;
    }

    /**
     * Genera la representación de siglas de los programas de la electiva.
     *
     * @param programas Lista de nombres de programas
     * @return Formato "[sigla1-sigla2-...]" o "[sigla]" si es uno solo
     */
    private String generarSiglasProgramas(List<String> programas) {
        if (programas == null || programas.isEmpty()) return "[]";

        String siglas = programas.stream()
                .map(this::obtenerSigla)
                .collect(Collectors.joining("-"));

        return "[" + siglas + "]";
    }

    /**
     * Convierte un nombre de programa en sigla (ej: "Ingeniería de Sistemas" → "PIS").
     */
    private String obtenerSigla(String nombre) {
        if (nombre == null || nombre.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("P"); // siempre empieza con P
        Arrays.stream(nombre.split("\\s+"))
                .filter(p -> !p.equalsIgnoreCase("de") && !p.equalsIgnoreCase("y") && !p.equalsIgnoreCase("en"))
                .forEach(p -> sb.append(Character.toUpperCase(p.charAt(0))));
        return sb.toString();
    }
}

