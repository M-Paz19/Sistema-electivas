package com.unicauca.fiet.sistema_electivas.departamento.repository;

import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;
import com.unicauca.fiet.sistema_electivas.departamento.enums.EstadoDepartamento; // Importar el Enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    /**
     * Busca un departamento por su código exacto.
     *
     * @param codigo código único del departamento.
     * @return un Optional con el departamento encontrado, o vacío si no existe.
     */
    Optional<Departamento> findByCodigo(String codigo);

    /**
     * Busca un departamento por su nombre exacto.
     *
     * @param nombre nombre del departamento.
     * @return un Optional con el departamento, o vacío si no existe coincidencia.
     */
    Optional<Departamento> findByNombre(String nombre);

    /**
     * Obtiene todos los departamentos filtrados por un estado específico.
     *
     * @param estado estado del departamento (ACTIVO, INACTIVO, etc.).
     * @return lista de departamentos que cumplen el estado indicado.
     */
    List<Departamento> findByEstado(EstadoDepartamento estado);

    /**
     * Busca departamentos cuyo nombre o código contenga el texto indicado,
     * ignorando mayúsculas y minúsculas.
     *
     * <p>Útil para búsquedas rápidas o autocompletado.</p>
     *
     * @param nombre fragmento del nombre a buscar.
     * @param codigo fragmento del código a buscar.
     * @return lista de departamentos coincidentes según nombre o código.
     */
    List<Departamento> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo);
}