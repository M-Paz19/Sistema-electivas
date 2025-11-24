package com.unicauca.fiet.sistema_electivas.asignacion.service;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteOrdenamientoResponse;
import com.unicauca.fiet.sistema_electivas.asignacion.mapper.OrdenamientoMapper;
import com.unicauca.fiet.sistema_electivas.asignacion.repository.AsignacionElectivaRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.repository.DatosAcademicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultaAsignacionServiceImpl implements ConsultaAsignacionService {

    private final AsignacionElectivaRepository asignacionElectivaRepository;

    private final OrdenamientoMapper ordenamientoMapper; // para convertir entidades a DTOs

    @Autowired
    private DatosAcademicoRepository datosAcademicoRepository;


    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public List<EstudianteOrdenamientoResponse> obtenerAptosOrdenados(Long periodoId) {

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
    public List<DatosAcademico> obtenerAptosOrdenadosInterno(Long periodoId) {

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
}
