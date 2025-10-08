package com.unicauca.fiet.sistema_electivas.electiva.service;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;
import com.unicauca.fiet.sistema_electivas.departamento.enums.EstadoDepartamento;
import com.unicauca.fiet.sistema_electivas.departamento.repository.DepartamentoRepository;
import com.unicauca.fiet.sistema_electivas.electiva.dto.ActualizarElectivaDTO;
import com.unicauca.fiet.sistema_electivas.electiva.dto.CrearElectivaDTO;
import com.unicauca.fiet.sistema_electivas.electiva.dto.ElectivaResponseDTO;
import com.unicauca.fiet.sistema_electivas.electiva.mapper.ElectivaMapper;
import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.electiva.model.ProgramaElectiva;
import com.unicauca.fiet.sistema_electivas.electiva.model.ProgramaElectivaId;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ElectivaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.electiva.enums.EstadoElectiva; // Importar el Enum
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.ElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.ElectivaOfertadaRepository;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ProgramaElectivaRepository;
import com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import com.unicauca.fiet.sistema_electivas.programa.repository.ProgramaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ElectivaServiceImpl implements ElectivaService {

    @Autowired
    private ElectivaRepository electivaRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;
    @Autowired
    private ProgramaRepository programaRepository;
    @Autowired
    private ProgramaElectivaRepository programaElectivaRepository;
    @Autowired
    private ElectivaOfertadaRepository electivaOfertadaRepository;
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ElectivaResponseDTO crearElectiva(CrearElectivaDTO dto) {
        // Validaciones básicas de unicidad
        if (electivaRepository.findByCodigo(dto.getCodigo()).isPresent()) {
            throw new DuplicateResourceException("El código " + dto.getCodigo() + " ya está en uso.");
        }
        if (electivaRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new DuplicateResourceException("El nombre " + dto.getNombre() + " ya está en uso.");
        }

        // Validar y obtener departamento
        Departamento departamento = departamentoRepository.findById(dto.getDepartamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + dto.getDepartamentoId()));
        // Validar que el departamento esté activo antes de asignarlo
        if (departamento.getEstado() != EstadoDepartamento.ACTIVO) {
            throw new BusinessException("El departamento seleccionado no está activo.");
        }
        // Normalizar y validar lista de programas (eliminar duplicados manteniendo orden)
        Set<Long> programaIds = new LinkedHashSet<>(dto.getProgramasIds() != null ? dto.getProgramasIds() : Collections.emptyList());
        if (programaIds.isEmpty()) {
            throw new BusinessException("Debe asignar al menos un programa.");
        }

        // Cargar programas en lote y verificar existencia
        List<Programa> programas = programaRepository.findAllById(programaIds);
        if (programas.size() != programaIds.size()) {
            Set<Long> encontrados = programas.stream().map(Programa::getId).collect(Collectors.toSet());
            Set<Long> faltantes = programaIds.stream().filter(id -> !encontrados.contains(id)).collect(Collectors.toSet());
            throw new ResourceNotFoundException("Programas no encontrados: " + faltantes);
        }

        // Verificar que todos los programas estén en estado APROBADO
        List<Long> noAprobados = programas.stream()
                .filter(p -> p.getEstado() != EstadoPrograma.APROBADO)
                .map(Programa::getId)
                .collect(Collectors.toList());
        if (!noAprobados.isEmpty()) {
            throw new BusinessException("Los siguientes programas no están en estado APROBADO: " + noAprobados);
        }

        // Crear la electiva y persistirla
        Electiva electiva = ElectivaMapper.toEntity(dto, departamento);
        electiva.setEstado(EstadoElectiva.BORRADOR);
        Electiva nuevaElectiva = electivaRepository.save(electiva);

        // Crear relaciones ProgramaElectiva en lote
        List<ProgramaElectiva> relaciones = programas.stream()
                .map(p -> {
                    ProgramaElectiva pe = new ProgramaElectiva();
                    pe.setId(new ProgramaElectivaId(p.getId(), nuevaElectiva.getId()));
                    pe.setPrograma(p);
                    pe.setElectiva(nuevaElectiva);
                    return pe;
                })
                .collect(Collectors.toList());
        programaElectivaRepository.saveAll(relaciones);

        return ElectivaMapper.toResponse(nuevaElectiva);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ElectivaResponseDTO actualizarElectiva(Long id, ActualizarElectivaDTO dto) {
        Electiva electiva = electivaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electiva no encontrada con id: " + id));

        boolean tieneHistorial = electivaOfertadaRepository.hasHistorial(id);

        // Validar cambios únicos (código/nombre) solo si se intenta cambiar
        if (dto.getCodigo() != null && !dto.getCodigo().equals(electiva.getCodigo())) {
            if (electivaRepository.findByCodigo(dto.getCodigo()).isPresent()) {
                throw new DuplicateResourceException("El código [" + dto.getCodigo() + "] ya está en uso.");
            }
        }
        if (dto.getNombre() != null && !dto.getNombre().equals(electiva.getNombre())) {
            if (electivaRepository.findByNombre(dto.getNombre()).isPresent()) {
                throw new DuplicateResourceException("El nombre [" + dto.getNombre() + "] ya está en uso.");
            }
        }

        // Si no tiene historial, se permiten cambios estructurales
        if (!tieneHistorial) {
            if (dto.getCodigo() != null) electiva.setCodigo(dto.getCodigo());
            if (dto.getNombre() != null) electiva.setNombre(dto.getNombre());
            if (dto.getDescripcion() != null) electiva.setDescripcion(dto.getDescripcion());
            if (dto.getDepartamentoId() != null) {
                Departamento departamento = departamentoRepository.findById(dto.getDepartamentoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + dto.getDepartamentoId()));
                // Validar que el departamento esté activo antes de asignarlo
                if (departamento.getEstado() != EstadoDepartamento.ACTIVO) {
                    throw new BusinessException("El departamento seleccionado no está activo.");
                }

                electiva.setDepartamento(departamento);
            }
        }

        // Programas: siempre requeridos
        Set<Long> programaIds = dto.getProgramasIds() != null
                ? new LinkedHashSet<>(dto.getProgramasIds())
                : Collections.emptySet();
        if (programaIds.isEmpty()) {
            throw new BusinessException("Debe asignar al menos un programa.");
        }

        // Cargar programas en lote y validar existencia
        List<Programa> programas = programaRepository.findAllById(programaIds);
        if (programas.size() != programaIds.size()) {
            Set<Long> encontrados = programas.stream().map(Programa::getId).collect(Collectors.toSet());
            Set<Long> faltantes = programaIds.stream().filter(idProgramas -> !encontrados.contains(idProgramas)).collect(Collectors.toSet());
            throw new ResourceNotFoundException("Programas no encontrados: " + faltantes);
        }

        // Verificar que todos los programas estén en estado APROBADO
        List<Long> noAprobados = programas.stream()
                .filter(p -> p.getEstado() != EstadoPrograma.APROBADO)
                .map(Programa::getId)
                .collect(Collectors.toList());
        if (!noAprobados.isEmpty()) {
            throw new BusinessException("Los siguientes programas no están en estado APROBADO: " + noAprobados);
        }

        // Reemplazar asociaciones de programas (elimino y creo de nuevo)
        programaElectivaRepository.deleteByElectivaId(electiva.getId());

        List<ProgramaElectiva> relaciones = programas.stream()
                .map(p -> {
                    ProgramaElectiva pe = new ProgramaElectiva();
                    pe.setId(new ProgramaElectivaId(p.getId(), electiva.getId()));
                    pe.setPrograma(p);
                    pe.setElectiva(electiva);
                    return pe;
                })
                .collect(Collectors.toList());
        programaElectivaRepository.saveAll(relaciones);

        Electiva electivaActualizada = electivaRepository.save(electiva);
        return ElectivaMapper.toResponse(electivaActualizada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void desactivarElectiva(Long id) {
        Electiva electiva = electivaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electiva no encontrada con id: " + id));

        // Solo se puede desactivar si está APROBADA o ACTIVA
        if (electiva.getEstado() != EstadoElectiva.APROBADA) {
            throw new InvalidStateException("Solo se pueden desactivar electivas en estado APROBADA.");
        }

        // Verificar si tiene ofertas activas (estado OFERTADA)
        Optional<ElectivaOfertada> ofertaActiva = electivaOfertadaRepository.findFirstByElectivaIdAndEstado(
                id, EstadoElectivaOfertada.OFERTADA);

        if (ofertaActiva.isPresent()) {
            String periodo = ofertaActiva.get().getPeriodo().getSemestre();
            throw new InvalidStateException(
                    "No se puede desactivar. La electiva está actualmente ofertada en el periodo " + periodo +
                            ". Primero debe cerrar el periodo o remover la electiva de la oferta activa.");
        }

        // Si no tiene ofertas activas, se puede desactivar
        electiva.setEstado(EstadoElectiva.INACTIVA);
        electivaRepository.save(electiva);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void reactivarElectiva(Long id) {
        Electiva electiva = electivaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Electiva no encontrada"));
        electiva.setEstado(EstadoElectiva.APROBADA);
        electivaRepository.save(electiva);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void aprobarElectiva(Long id) {
        Electiva electiva = electivaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Electiva no encontrada"));
        if (!electiva.getEstado().equals(EstadoElectiva.BORRADOR)) {
            throw new InvalidStateException("Solo se pueden aprobar electivas en estado BORRADOR.");
        }
        electiva.setEstado(EstadoElectiva.APROBADA);
        electivaRepository.save(electiva);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ElectivaResponseDTO> findElectivas(boolean mostrarInactivas, String query) {
        List<Electiva> resultado;
        if (query != null && !query.trim().isEmpty()) {
            resultado = electivaRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(query, query);
        } else {
            resultado = electivaRepository.findAll();
        }

        // ⚙️ Filtrar inactivas si no deben mostrarse
        if (!mostrarInactivas) {
            resultado = resultado.stream()
                    .filter(e -> e.getEstado() != EstadoElectiva.INACTIVA)
                    .collect(Collectors.toList());
        }

        return resultado.stream()
                .map(ElectivaMapper::toResponse)
                .collect(Collectors.toList());
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ElectivaResponseDTO buscarPorId(Long id) {
        Electiva electiva = electivaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electiva no encontrada con id: " + id));
        return ElectivaMapper.toResponse(electiva);
    }
}