package com.unicauca.fiet.sistema_electivas.departamento.repository;

import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;
import com.unicauca.fiet.sistema_electivas.departamento.enums.EstadoDepartamento; // Importar el Enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    Optional<Departamento> findByCodigo(String codigo);

    Optional<Departamento> findByNombre(String nombre);

    List<Departamento> findByEstado(EstadoDepartamento estado);

    List<Departamento> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo);
}