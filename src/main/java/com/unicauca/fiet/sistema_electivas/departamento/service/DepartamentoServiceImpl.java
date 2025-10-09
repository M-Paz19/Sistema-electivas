package com.unicauca.fiet.sistema_electivas.departamento.service;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.departamento.dto.DepartamentoRequestDTO;
import com.unicauca.fiet.sistema_electivas.departamento.dto.DepartamentoResponse;
import com.unicauca.fiet.sistema_electivas.departamento.enums.EstadoDepartamento;
import com.unicauca.fiet.sistema_electivas.departamento.mapper.DepartamentoMapper;
import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;
import com.unicauca.fiet.sistema_electivas.electiva.enums.EstadoElectiva;
import com.unicauca.fiet.sistema_electivas.departamento.repository.DepartamentoRepository;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ElectivaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ElectivaRepository electivaRepository;
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public DepartamentoResponse crearDepartamento(DepartamentoRequestDTO dto) {
        // 🔹 Validaciones básicas
        if (dto.getCodigo() == null || dto.getCodigo().isBlank() ||
                dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new BusinessException("Complete todos los campos obligatorios.");
        }

        // 🔹 Validar duplicados
        if (departamentoRepository.findByCodigo(dto.getCodigo()).isPresent()) {
            throw new DuplicateResourceException("El código '" + dto.getCodigo() + "' ya está en uso. Por favor, utilice otro.");
        }

        if (departamentoRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new DuplicateResourceException("El nombre '" + dto.getNombre() + "' ya está en uso. Por favor, utilice otro.");
        }

        // 🔹 Convertir el DTO a entidad usando el Mapper
        Departamento departamento = DepartamentoMapper.toEntity(dto);
        departamento.setEstado(EstadoDepartamento.ACTIVO);

        // 🔹 Guardar en BD
        Departamento saved = departamentoRepository.save(departamento);

        // 🔹 Retornar respuesta limpia
        return DepartamentoMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public DepartamentoResponse actualizarDepartamento(Long id, DepartamentoRequestDTO dto) {
        // 🔹 Buscar el departamento existente
        Departamento deptoExistente = departamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado con id: " + id));

        // 🔹 Validar estado
        if (deptoExistente.getEstado() == EstadoDepartamento.INACTIVO) {
            throw new InvalidStateException("No se puede modificar un departamento inactivo.");
        }

        // 🔹 Validar campos requeridos
        if (dto.getCodigo() == null || dto.getCodigo().isBlank() ||
                dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new BusinessException("Complete todos los campos obligatorios.");
        }

        // 🔹 Validar duplicados (código y nombre en uso por otro departamento)
        departamentoRepository.findByCodigo(dto.getCodigo()).ifPresent(d -> {
            if (!d.getId().equals(id)) {
                throw new DuplicateResourceException("El código '" + dto.getCodigo() + "' ya está en uso por otro departamento.");
            }
        });

        departamentoRepository.findByNombre(dto.getNombre()).ifPresent(d -> {
            if (!d.getId().equals(id)) {
                throw new DuplicateResourceException("El nombre '" + dto.getNombre() + "' ya está en uso por otro departamento.");
            }
        });

        // 🔹 Actualizar campos
        deptoExistente.setCodigo(dto.getCodigo());
        deptoExistente.setNombre(dto.getNombre());
        deptoExistente.setDescripcion(dto.getDescripcion());

        // 🔹 Guardar y mapear respuesta
        Departamento actualizado = departamentoRepository.save(deptoExistente);
        return DepartamentoMapper.toResponse(actualizado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deshabilitarDepartamento(Long id) {
        Departamento depto = departamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado con id: " + id));

        if (depto.getEstado() != EstadoDepartamento.ACTIVO) {
            throw new InvalidStateException("Solo se pueden deshabilitar departamentos activos.");
        }

        boolean tieneElectivasActivas = electivaRepository.existsByDepartamentoAndEstado(depto, EstadoElectiva.APROBADA);
        if (tieneElectivasActivas) {
            throw new InvalidStateException(
                    "No se puede deshabilitar el departamento porque tiene electivas activas. " +
                            "Reasigne o desactive las electivas primero."
            );
        }

        depto.setEstado(EstadoDepartamento.INACTIVO);
        departamentoRepository.save(depto);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<DepartamentoResponse> findDepartamentos(String filtroEstado, String query) {
        List<Departamento> departamentos;

        if (query != null && !query.trim().isEmpty()) {
            departamentos = departamentoRepository
                    .findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(query, query);
        } else if (filtroEstado != null && !filtroEstado.equalsIgnoreCase("TODOS")) {
            departamentos = departamentoRepository
                    .findByEstado(EstadoDepartamento.valueOf(filtroEstado.toUpperCase()));
        } else {
            departamentos = departamentoRepository.findAll();
        }

        // mapear entidades → responses
        return departamentos.stream()
                .map(DepartamentoMapper::toResponse)
                .toList();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public DepartamentoResponse buscarPorId(Long id) {
        Departamento depto = departamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado con id: " + id));
        return DepartamentoMapper.toResponse(depto);
    }



}