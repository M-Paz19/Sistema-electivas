package com.unicauca.fiet.sistema_electivas.electiva.repository;

import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;
import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.electiva.enums.EstadoElectiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ElectivaRepository extends JpaRepository<Electiva, Long> {

    Optional<Electiva> findByCodigo(String codigo);

    Optional<Electiva> findByNombre(String nombre);

    boolean existsByDepartamentoAndEstado(Departamento departamento, EstadoElectiva estado);

    List<Electiva> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo);


}