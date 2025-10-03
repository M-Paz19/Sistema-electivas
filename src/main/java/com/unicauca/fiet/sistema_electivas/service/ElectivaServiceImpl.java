package com.unicauca.fiet.sistema_electivas.service;

import com.unicauca.fiet.sistema_electivas.model.Electiva;
import com.unicauca.fiet.sistema_electivas.model.Electiva.EstadoElectiva; // Importar el Enum
import com.unicauca.fiet.sistema_electivas.repository.ElectivaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElectivaServiceImpl implements ElectivaService {

    @Autowired
    private ElectivaRepository electivaRepository;

    @Override
    public Electiva crearElectiva(Electiva electiva) {
        if (electiva.getCodigo() == null || electiva.getNombre() == null || electiva.getCodigo().isBlank() || electiva.getNombre().isBlank() || electiva.getDepartamento() == null) {
            throw new IllegalArgumentException("Complete todos los campos obligatorios.");
        }

        if (electivaRepository.findByCodigo(electiva.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("El código " + electiva.getCodigo() + " ya está en uso.");
        }
        if (electivaRepository.findByNombre(electiva.getNombre()).isPresent()) {
            throw new IllegalArgumentException("El nombre " + electiva.getNombre() + " ya está en uso.");
        }

        electiva.setEstado(EstadoElectiva.BORRADOR);
        return electivaRepository.save(electiva);
    }

    @Override
    public Electiva actualizarElectiva(Long id, Electiva electivaDetails) {
        Electiva electivaExistente = electivaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Electiva no encontrada con id: " + id));

        electivaExistente.setNombre(electivaDetails.getNombre());
        electivaExistente.setDescripcion(electivaDetails.getDescripcion());
        electivaExistente.setDepartamento(electivaDetails.getDepartamento());

        return electivaRepository.save(electivaExistente);
    }

    @Override
    public void desactivarElectiva(Long id) {
        Electiva electiva = electivaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Electiva no encontrada"));

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