package com.unicauca.fiet.sistema_electivas.programa.repository;

import com.unicauca.fiet.sistema_electivas.programa.enums.EstadoPrograma;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades {@link Programa}.
 * <p>
 * Extiende de {@link JpaRepository} para proporcionar operaciones CRUD
 * y define consultas personalizadas para buscar programas por
 * código o nombre.
 */
@Repository
public interface ProgramaRepository extends JpaRepository<Programa, Long> {

    /**
     * Busca un programa por su código único.
     *
     * @param codigo código del programa
     * @return un {@link Optional} que contiene el programa si existe,
     *         o vacío en caso contrario
     */
    Optional<Programa> findByCodigo(String codigo);

    /**
     * Busca un programa por su nombre.
     *
     * @param nombre nombre del programa
     * @return un {@link Optional} que contiene el programa si existe,
     *         o vacío en caso contrario
     */
    Optional<Programa> findByNombre(String nombre);
    /**
     * Devuelve todos los programas con un estado específico.
     *
     * @param estado estado del programa
     * @return lista de programas con ese estado
     */
    List<Programa> findByEstado(EstadoPrograma estado);

    /**
     * Devuelve los programas cuyo nombre contenga el texto indicado (búsqueda parcial).
     *
     * @param nombre nombre o parte del nombre
     * @return lista de programas coincidentes
     */
    List<Programa> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Devuelve los programas cuyo código contenga el texto indicado (búsqueda parcial).
     *
     * @param codigo código o parte del código
     * @return lista de programas coincidentes
     */
    List<Programa> findByCodigoContainingIgnoreCase(String codigo);
    /**
     * Devuelve opcionalemnte un programa en vase al nombre
     *
     * @param nombre nombre para buscar ele programa
     * @return el programa opcional encontrado
     */
    Optional<Programa> findByNombreIgnoreCase(String nombre);

    @Query("""
    SELECT p FROM Programa p
    WHERE CAST(function('unaccent', lower(p.nombre)) AS string)
          LIKE CAST(function('unaccent', lower(CONCAT('%', :nombre, '%'))) AS string)
      AND p.estado = :estado
""")
    Optional<Programa> buscarFlexible(String nombre, EstadoPrograma estado);


}
