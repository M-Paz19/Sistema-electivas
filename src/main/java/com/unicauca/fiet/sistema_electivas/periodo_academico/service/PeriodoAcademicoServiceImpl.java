package com.unicauca.fiet.sistema_electivas.periodo_academico.service;

import com.unicauca.fiet.sistema_electivas.electiva.enums.EstadoElectiva;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.AgregarElectivaOfertadaDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.CrearPeriodoAcademicoDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.ElectivaOfertadaResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.PeriodoAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.ElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.electiva.model.ProgramaElectiva;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.ElectivaOfertadaRepository;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ElectivaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ProgramaElectivaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PeriodoAcademicoServiceImpl implements PeriodoAcademicoService {

    private static final Pattern SEMESTRE_PATTERN = Pattern.compile("^20\\d{2}-(1|2)$");
    @Autowired
    private PeriodoAcademicoRepository periodoRepository;
    @Autowired
    private ElectivaRepository electivaRepository;
    @Autowired
    private ElectivaOfertadaRepository electivaOfertadaRepository;
    @Autowired
    private ProgramaElectivaRepository programaElectivaRepository;
    @Override
    @Transactional
    public PeriodoAcademicoResponse crearPeriodo(CrearPeriodoAcademicoDTO dto) {

        // 1. Validar duplicado
        periodoRepository.findBySemestre(dto.getSemestre()).ifPresent(p -> {
            throw new DuplicateResourceException(
                    "El semestre [" + dto.getSemestre() + "] ya existe. Por favor, utilice otro identificador."
            );
        });

        // 2. Validar formato del semestre
        if (!SEMESTRE_PATTERN.matcher(dto.getSemestre()).matches()) {
            throw new BusinessException("El formato del semestre debe ser '20XX-1' o '20XX-2'.");
        }

        // 3. Validar fechas coherentes
        Instant apertura = dto.getFechaApertura();
        Instant cierre = dto.getFechaCierre();

        if (apertura.isAfter(cierre)) {
            throw new BusinessException("La fecha de apertura no puede ser posterior a la de cierre.");
        }

        // 4. Convertir a LocalDate para extraer año y mes
        ZoneId zona = ZoneId.systemDefault();
        LocalDate aperturaFecha = LocalDate.ofInstant(apertura, zona);
        LocalDate cierreFecha = LocalDate.ofInstant(cierre, zona);

        String[] partes = dto.getSemestre().split("-");
        int anio = Integer.parseInt(partes[0]);
        int semestreNum = Integer.parseInt(partes[1]);


        if (semestreNum == 1) {
            // Ejemplo: 2026-1 → Apertura desde julio 2025 hasta cierre junio 2026
            LocalDate inicioPermitido = LocalDate.of(anio - 1, 7, 1);
            LocalDate finPermitido = LocalDate.of(anio, 6, 30);

            if (aperturaFecha.isBefore(inicioPermitido) || cierreFecha.isAfter(finPermitido)) {
                throw new BusinessException(
                        String.format(
                                "Las fechas del semestre %d-%d deben estar entre %s y %s.",
                                anio, semestreNum,
                                inicioPermitido, finPermitido
                        )
                );
            }
        } else {
            // Ejemplo: 2026-2 → Apertura y cierre dentro del mismo año (julio a diciembre)
            LocalDate inicioPermitido = LocalDate.of(anio, 1, 1);
            LocalDate finPermitido = LocalDate.of(anio, 12, 31);

            if (aperturaFecha.isBefore(inicioPermitido) || cierreFecha.isAfter(finPermitido)) {
                throw new BusinessException(
                        String.format(
                                "Las fechas del semestre %d-%d deben estar entre %s y %s.",
                                anio, semestreNum,
                                inicioPermitido, finPermitido
                        )
                );
            }
        }
        // 6. Crear y guardar
        PeriodoAcademico nuevo = new PeriodoAcademico();
        nuevo.setSemestre(dto.getSemestre());
        nuevo.setFechaApertura(apertura);
        nuevo.setFechaCierre(cierre);
        nuevo.setEstado(EstadoPeriodoAcademico.CONFIGURACION);
        periodoRepository.save(nuevo);
        return PeriodoAcademicoResponse.fromEntity(
                nuevo,
                String.format("Período académico %s creado exitosamente en estado %s", nuevo.getSemestre(), nuevo.getEstado().getDescripcion())
        );
    }

    @Transactional
    public ElectivaOfertadaResponse agregarElectivaOfertada(Long periodoId, AgregarElectivaOfertadaDTO dto) {
        // 1. Validar período
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));
        if (periodo.getEstado() != EstadoPeriodoAcademico.CONFIGURACION) {
            throw new InvalidStateException("No se pueden agregar electivas en un período no configurable.");
        }

        // 2. Validar electiva
        Electiva electiva = electivaRepository.findById(dto.getElectivaId())
                .orElseThrow(() -> new ResourceNotFoundException("Electiva no encontrada"));
        if (electiva.getEstado() != EstadoElectiva.APROBADA) {
            throw new BusinessException("Solo se pueden ofertar electivas aprobadas.");
        }

        // 3. Evitar duplicados
        boolean yaExiste = electivaOfertadaRepository.existsByElectivaIdAndPeriodoId(electiva.getId(), periodo.getId());
        if (yaExiste) {
            throw new BusinessException("La electiva " + electiva.getCodigo() + " - " + electiva.getNombre() +
                    " ya se encuentra en la oferta de este período.");
        }

        // 4. Copiar configuración de programas
        Map<Long, Integer> cupos = validarYCopiarCupos(dto.getCuposPorPrograma(), electiva);

        // 5. Crear y guardar oferta
        ElectivaOfertada ofertada = new ElectivaOfertada();
        ofertada.setElectiva(electiva);
        ofertada.setPeriodo(periodo);
        ofertada.setCuposPorPrograma(cupos);
        ofertada.setEstado(EstadoElectivaOfertada.OFERTADA);
        ofertada.setFechaCreacion(Instant.now());
        ofertada.setFechaActualizacion(Instant.now());

        ElectivaOfertada guardada = electivaOfertadaRepository.save(ofertada);

        // 6. Armar response elegante
        return ElectivaOfertadaResponse.builder()
                .id(guardada.getId())
                .electivaId(electiva.getId())
                .codigoElectiva(electiva.getCodigo())
                .nombreElectiva(electiva.getNombre())
                .periodoId(periodo.getId())
                .nombrePeriodo(periodo.getSemestre())
                .estado(guardada.getEstado())
                .cuposPorPrograma(guardada.getCuposPorPrograma())
                .fechaCreacion(guardada.getFechaCreacion())
                .fechaActualizacion(guardada.getFechaActualizacion())
                .build();
    }


    private Map<Long, Integer> validarYCopiarCupos(Map<Long, Integer> cuposDto, Electiva electiva) {
        Map<Long, Integer> cuposFinales = new HashMap<>();

        //  1. Obtener los programas asociados a la electiva desde la tabla intermedia
        List<ProgramaElectiva> asociaciones = programaElectivaRepository.findByElectivaId(electiva.getId());

        if (asociaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "La electiva no tiene programas asociados en programa_electiva.");
        }

        Set<Long> programasAsociados = asociaciones.stream()
                .map(pe -> pe.getPrograma().getId())
                .collect(Collectors.toSet());

        // 2. Si no hay cupos definidos, repartir los 18 equitativamente
        if (cuposDto == null || cuposDto.isEmpty()) {
            int totalProgramas = programasAsociados.size();
            int cuposPorPrograma = 18 / totalProgramas;
            int residuo = 18 % totalProgramas;

            for (Long idPrograma : programasAsociados) {
                cuposFinales.put(idPrograma, cuposPorPrograma);
            }

            // Asignar el residuo (si lo hay) al primer programa
            if (residuo > 0) {
                Long primerPrograma = programasAsociados.iterator().next();
                cuposFinales.put(primerPrograma, cuposFinales.get(primerPrograma) + residuo);
            }

            return cuposFinales;
        }

        // 3. Validar los cupos enviados desde el DTO
        int sumaTotal = 0;
        for (Map.Entry<Long, Integer> entry : cuposDto.entrySet()) {
            Long programaId = entry.getKey();
            Integer cupos = entry.getValue();

            // Validar tipos y nulos
            if (programaId == null || cupos == null) {
                throw new IllegalArgumentException("Los IDs de programa y cupos no pueden ser nulos.");
            }

            // Validar asociación con la electiva
            if (!programasAsociados.contains(programaId)) {
                throw new IllegalArgumentException(
                        String.format("El programa con ID %d no está asociado a la electiva.", programaId)
                );
            }

            // Validar cupos positivos
            if (cupos <= 0) {
                throw new IllegalArgumentException(
                        String.format("El número de cupos para el programa %d debe ser mayor que 0.", programaId)
                );
            }

            cuposFinales.put(programaId, cupos);
            sumaTotal += cupos;
        }

        // 4. Validar suma total de 18 cupos exactos
        if (sumaTotal != 18) {
            throw new IllegalArgumentException(
                    String.format("La suma total de cupos (%d) debe ser exactamente 18.", sumaTotal)
            );
        }

        return cuposFinales;
    }

}
