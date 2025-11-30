package com.unicauca.fiet.sistema_electivas.archivo.repository;


import com.unicauca.fiet.sistema_electivas.archivo.enums.TipoArchivo;
import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para el manejo de operaciones de persistencia sobre la entidad {@link CargaArchivo}.
 *
 * <p>Permite realizar consultas y operaciones CRUD sobre los archivos cargados
 * en el sistema, incluyendo los lotes de códigos generados para SIMCA.</p>
 */
@Repository
public interface CargaArchivoRepository extends JpaRepository<CargaArchivo, Long> {
    /**
     * Busca todos los archivos asociados a un período académico y de un tipo específico.
     *
     * <p>Este método se usa, por ejemplo, para recuperar los archivos de tipo
     * {@link TipoArchivo#LOTES_CODIGOS} generados para SIMCA en un período determinado.</p>
     *
     * @param periodoId   ID del período académico
     * @param tipoArchivo tipo de archivo a buscar (por ejemplo LOTES_CODIGOS)
     * @return lista de archivos que coinciden con los criterios
     */
    List<CargaArchivo> findByPeriodoIdAndTipoArchivo(Long periodoId, TipoArchivo tipoArchivo);

    /**
     * Cuenta cuántos archivos de un tipo específico existen para un período dado.
     *
     * <p>Se usa para validaciones como:
     * verificar si ya existe un lote cargado antes de generar uno nuevo.</p>
     *
     * @param periodo      entidad PeriodoAcademico
     * @param tipoArchivo  tipo de archivo
     * @return número de coincidencias
     */
    int countByPeriodoAndTipoArchivo(PeriodoAcademico periodo, TipoArchivo tipoArchivo);

    /**
     * Obtiene el archivo más reciente de un tipo específico dentro de un período.
     *
     * <p>Este método se usa para obtener siempre la última versión disponible
     * (ordenada por fecha de carga descendente).</p>
     *
     * @param periodoId   ID del período académico
     * @param tipoArchivo tipo de archivo
     * @return último archivo cargado (si existe)
     */
    Optional<CargaArchivo> findTopByPeriodoIdAndTipoArchivoOrderByFechaCargaDesc(
            Long periodoId,
            TipoArchivo tipoArchivo
    );


}
