package com.unicauca.fiet.sistema_electivas.asignacion.mapper;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.DepartamentoReporteDTO;
import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteAsignacionDTO;
import com.unicauca.fiet.sistema_electivas.asignacion.dto.OfertaReporteDTO;
import com.unicauca.fiet.sistema_electivas.asignacion.enums.EstadoAsignacion;
import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;

import java.util.List;

public class ReporteAsignacionMapper {

    public static DepartamentoReporteDTO toDepartamentoDTO(Departamento d) {
        return new DepartamentoReporteDTO(d.getId(), d.getCodigo(), d.getNombre());
    }

    public static OfertaReporteDTO toOfertaDTO(Oferta o, List<String> programas, List<EstudianteAsignacionDTO> estudiantes) {
        OfertaReporteDTO dto = new OfertaReporteDTO();
        dto.setIdOferta(o.getId());
        dto.setCodigoElectiva(o.getElectiva().getCodigo());
        dto.setNombreElectiva(o.getElectiva().getNombre());
        dto.setProgramas(programas);
        dto.setListaEstudiantes(estudiantes);
        return dto;
    }

    public static EstudianteAsignacionDTO toEstudianteDTO(DatosAcademico d, EstadoAsignacion estado, int numero) {
        EstudianteAsignacionDTO dto = new EstudianteAsignacionDTO();
        dto.setNumero(numero);
        dto.setCodigo(d.getCodigoEstudiante());
        dto.setApellidos(d.getApellidos());
        dto.setNombres(d.getNombres());
        dto.setUsuario(d.getUsuario());
        dto.setEsNivelado(d.getEsNivelado());
        dto.setPorcentajeAvance(d.getPorcentajeAvance());
        dto.setEstado(estado);
        return dto;
    }
}
