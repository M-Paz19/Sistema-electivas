package com.unicauca.fiet.sistema_electivas.service;

import com.unicauca.fiet.sistema_electivas.model.Departamento;
import com.unicauca.fiet.sistema_electivas.model.Departamento.EstadoDepartamento;
import com.unicauca.fiet.sistema_electivas.model.Electiva.EstadoElectiva;
import com.unicauca.fiet.sistema_electivas.repository.DepartamentoRepository;
import com.unicauca.fiet.sistema_electivas.repository.ElectivaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ElectivaRepository electivaRepository;

    @Override
    public Departamento crearDepartamento(Departamento departamento) {
        if (departamento.getCodigo() == null || departamento.getNombre() == null || departamento.getCodigo().isBlank() || departamento.getNombre().isBlank()) {
            throw new IllegalArgumentException("Complete todos los campos obligatorios.");
        }
        if (departamentoRepository.findByCodigo(departamento.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("El código " + departamento.getCodigo() + " ya está en uso. Por favor, utilice otro.");
        }
        if (departamentoRepository.findByNombre(departamento.getNombre()).isPresent()) {
            throw new IllegalArgumentException("El nombre " + departamento.getNombre() + " ya está en uso. Por favor, utilice otro.");
        }
        departamento.setEstado(EstadoDepartamento.ACTIVO);
        return departamentoRepository.save(departamento);
    }

    @Override
    public Departamento actualizarDepartamento(Long id, Departamento deptoDetails) {
        Departamento deptoExistente = departamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado con id: " + id));

        if (deptoDetails.getCodigo() == null || deptoDetails.getNombre() == null || deptoDetails.getCodigo().isBlank() || deptoDetails.getNombre().isBlank()) {
            throw new IllegalArgumentException("Complete todos los campos obligatorios.");
        }

        departamentoRepository.findByCodigo(deptoDetails.getCodigo()).ifPresent(d -> {
            if (!d.getId().equals(id)) {
                throw new IllegalArgumentException("El código " + deptoDetails.getCodigo() + " ya está en uso. Por favor, utilice otro.");
            }
        });
        departamentoRepository.findByNombre(deptoDetails.getNombre()).ifPresent(d -> {
            if (!d.getId().equals(id)) {
                throw new IllegalArgumentException("El nombre " + deptoDetails.getNombre() + " ya está en uso. Por favor, utilice otro.");
            }
        });

        deptoExistente.setCodigo(deptoDetails.getCodigo());
        deptoExistente.setNombre(deptoDetails.getNombre());
        deptoExistente.setDescripcion(deptoDetails.getDescripcion());

        return departamentoRepository.save(deptoExistente);
    }

    @Override
    public void deshabilitarDepartamento(Long id) {
        Departamento depto = departamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado con id: " + id));

        boolean tieneElectivasActivas = electivaRepository.existsByDepartamentoAndEstado(depto, EstadoElectiva.APROBADA);
        if(tieneElectivasActivas){
            throw new IllegalStateException("No se puede deshabilitar el departamento porque tiene electivas asociadas activas. Reasigne o desactive las electivas primero.");
        }

        depto.setEstado(EstadoDepartamento.INACTIVO);
        departamentoRepository.save(depto);
    }

    @Override
    public List<Departamento> findDepartamentos(String filtroEstado, String query) {
        if (query != null && !query.trim().isEmpty()) {
            return departamentoRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(query, query);
        }
        if (filtroEstado != null && !filtroEstado.equalsIgnoreCase("TODOS")) {
            return departamentoRepository.findByEstado(EstadoDepartamento.valueOf(filtroEstado.toUpperCase()));
        }
        return departamentoRepository.findAll();
    }
}