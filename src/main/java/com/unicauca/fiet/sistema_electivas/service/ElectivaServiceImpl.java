package com.unicauca.fiet.sistema_electivas.service;

import com.unicauca.fiet.sistema_electivas.dto.ActualizarElectivaDTO;
import com.unicauca.fiet.sistema_electivas.dto.CrearElectivaDTO;
import com.unicauca.fiet.sistema_electivas.dto.ElectivaResponseDTO;
import com.unicauca.fiet.sistema_electivas.enums.EstadoElectivaOfertada;
import com.unicauca.fiet.sistema_electivas.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.model.*;
import com.unicauca.fiet.sistema_electivas.model.Electiva.EstadoElectiva; // Importar el Enum
import com.unicauca.fiet.sistema_electivas.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
    @Transactional
    public ElectivaResponseDTO crearElectiva(CrearElectivaDTO dto) {
        // Validaciones básicas
        if (electivaRepository.findByCodigo(dto.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("El código " + dto.getCodigo() + " ya está en uso.");
        }
        if (electivaRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new IllegalArgumentException("El nombre " + dto.getNombre() + " ya está en uso.");
        }

        Departamento departamento = departamentoRepository.findById(dto.getDepartamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado"));

        Electiva electiva = new Electiva();
        electiva.setCodigo(dto.getCodigo());
        electiva.setNombre(dto.getNombre());
        electiva.setDescripcion(dto.getDescripcion());
        electiva.setDepartamento(departamento);
        electiva.setEstado(Electiva.EstadoElectiva.BORRADOR);

        // Guardar la electiva primero
        Electiva nuevaElectiva = electivaRepository.save(electiva);

        // Asociar programas
        for (long programaId : dto.getProgramasIds()) {
            Programa programa = programaRepository.findById(programaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Programa no encontrado: " + programaId));
            ProgramaElectiva pe = new ProgramaElectiva();
            pe.setId(new ProgramaElectivaId(programa.getId(), nuevaElectiva.getId()));
            pe.setPrograma(programa);
            pe.setElectiva(nuevaElectiva);

            programaElectivaRepository.save(pe);
        }


        return new ElectivaResponseDTO(
                nuevaElectiva.getId(),
                nuevaElectiva.getCodigo(),
                nuevaElectiva.getNombre(),
                nuevaElectiva.getDescripcion(),
                nuevaElectiva.getEstado().name(),
                nuevaElectiva.getDepartamento().getId(),
                nuevaElectiva.getDepartamento().getNombre(),
                "Electiva creada exitosamente en estado BORRADOR"
        );
    }

    @Transactional
    public Electiva actualizarElectiva(Long id, ActualizarElectivaDTO dto) {
        Electiva electiva = electivaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electiva no encontrada con id: " + id));

        boolean tieneHistorial = electivaOfertadaRepository.hasHistorial(id);

        // Validar duplicados solo si se intenta cambiar código/nombre
        if (dto.getCodigo() != null && !dto.getCodigo().equals(electiva.getCodigo())) {
            if (electivaRepository.findByCodigo(dto.getCodigo()).isPresent()) {
                throw new IllegalArgumentException("El código [" + dto.getCodigo() + "] ya está en uso.");
            }
        }

        if (dto.getNombre() != null && !dto.getNombre().equals(electiva.getNombre())) {
            if (electivaRepository.findByNombre(dto.getNombre()).isPresent()) {
                throw new IllegalArgumentException("El nombre [" + dto.getNombre() + "] ya está en uso.");
            }
        }

        // Aplicar cambios según estado
        if (!tieneHistorial) {
            if (dto.getCodigo() != null) electiva.setCodigo(dto.getCodigo());
            if (dto.getNombre() != null) electiva.setNombre(dto.getNombre());
            if (dto.getDescripcion() != null) electiva.setDescripcion(dto.getDescripcion());
            if (dto.getDepartamentoId() != null) {
                Departamento departamento = departamentoRepository.findById(dto.getDepartamentoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado"));
                electiva.setDepartamento(departamento);
            }
        }

        // Programas siempre se pueden actualizar
        if (dto.getProgramasIds() == null || dto.getProgramasIds().isEmpty()) {
            throw new IllegalArgumentException("Debe asignar al menos un programa con cupo.");
        }

        // Primero eliminamos los programas actuales y volvemos a guardar
        programaElectivaRepository.deleteByElectivaId(electiva.getId());

        for (Long progId : dto.getProgramasIds()) {
            Programa programa = programaRepository.findById(progId)
                    .orElseThrow(() -> new ResourceNotFoundException("Programa no encontrado: " + progId));
            ProgramaElectiva pe = new ProgramaElectiva();
            pe.setId(new ProgramaElectivaId(programa.getId(), electiva.getId()));
            pe.setPrograma(programa);
            pe.setElectiva(electiva);
            programaElectivaRepository.save(pe);
        }

        return electivaRepository.save(electiva);
    }


    @Override
    @Transactional
    public void desactivarElectiva(Long id) {
        Electiva electiva = electivaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Electiva no encontrada con id: " + id));

        // Solo se puede desactivar si está APROBADA o ACTIVA
        if (electiva.getEstado() != EstadoElectiva.APROBADA) {
            throw new IllegalArgumentException("Solo se pueden desactivar electivas en estado APROBADA.");
        }

        // Verificar si tiene ofertas activas (estado OFERTADA)
        Optional<ElectivaOfertada> ofertaActiva = electivaOfertadaRepository.findFirstByElectivaIdAndEstado(
                id, EstadoElectivaOfertada.OFERTADA);

        if (ofertaActiva.isPresent()) {
            String periodo = ofertaActiva.get().getPeriodo().getSemestre();
            throw new IllegalArgumentException(
                    "No se puede desactivar. La electiva está actualmente ofertada en el periodo " + periodo +
                            ". Primero debe cerrar el periodo o remover la electiva de la oferta activa.");
        }

        // Si no tiene ofertas activas, se puede desactivar
        electiva.setEstado(EstadoElectiva.INACTIVA);
        electivaRepository.save(electiva);
    }

    @Override
    public void reactivarElectiva(Long id) {
        Electiva electiva = electivaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Electiva no encontrada"));
        electiva.setEstado(EstadoElectiva.APROBADA);
        electivaRepository.save(electiva);
    }

    @Override
    public void aprobarElectiva(Long id) {
        Electiva electiva = electivaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Electiva no encontrada"));
        if (!electiva.getEstado().equals(EstadoElectiva.BORRADOR)) {
            throw new IllegalStateException("Solo se pueden aprobar electivas en estado BORRADOR.");
        }
        electiva.setEstado(EstadoElectiva.APROBADA);
        electivaRepository.save(electiva);
    }

    @Override
    public List<Electiva> findElectivas(boolean mostrarInactivas, String query) {
        List<Electiva> resultado;
        if (query != null && !query.trim().isEmpty()) {
            resultado = electivaRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(query, query);
        } else {
            resultado = electivaRepository.findAll();
        }

        if (mostrarInactivas) {
            return resultado;
        }

        return resultado.stream()
                .filter(e -> !e.getEstado().equals(EstadoElectiva.INACTIVA))
                .collect(Collectors.toList());
    }
}