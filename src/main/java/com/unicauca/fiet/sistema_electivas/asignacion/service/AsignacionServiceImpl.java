package com.unicauca.fiet.sistema_electivas.asignacion.service;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteOrdenamientoResponse;
import com.unicauca.fiet.sistema_electivas.asignacion.mapper.OrdenamientoMapper;
import com.unicauca.fiet.sistema_electivas.asignacion.service.AsignacionService;


import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;

import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;


import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CambioEstadoValidacionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.DatosAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.DatosAcademicoMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.ValidacionProcesamientoMapper;
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
@Slf4j
@RequiredArgsConstructor
public class AsignacionServiceImpl implements AsignacionService {
    @Autowired
    private PeriodoAcademicoRepository periodoRepository;
    @Autowired
    private DatosAcademicoRepository datosAcademicoRepository;
    @Autowired
    private OrdenamientoMapper ordamientoMapper;

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
                    "Solo se puede filtrar estudiantes no elegibles cuando el período está en estado EN_PROCESO_ASIGNACION."
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

        return ordamientoMapper.toResponseList(aptos);
    }

    /**
     * Obtiene y ordena internamente la lista de estudiantes aptos
     * aplicando los criterios oficiales establecidos por la universidad.
     *
     * <p>Este método es auxiliar y su propósito es permitir que otras
     * operaciones internas del servicio (como la asignación de cupos)
     * trabajen directamente con las entidades {@link DatosAcademico},
     * evitando transformaciones innecesarias a DTOs.</p>
     *
     * @param periodoId identificador del período académico
     * @return lista ordenada de entidades {@link DatosAcademico}
     */
    private List<DatosAcademico> obtenerAptosOrdenadosInterno(Long periodoId) {

        // Consultar todos los estudiantes APTO del período
        List<DatosAcademico> aptos =
                datosAcademicoRepository.findByRespuesta_PeriodoIdAndEstadoAptitud(
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