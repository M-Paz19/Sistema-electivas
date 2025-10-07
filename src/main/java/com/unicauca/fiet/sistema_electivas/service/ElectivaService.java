package com.unicauca.fiet.sistema_electivas.service;

import com.unicauca.fiet.sistema_electivas.dto.ActualizarElectivaDTO;
import com.unicauca.fiet.sistema_electivas.dto.CrearElectivaDTO;
import com.unicauca.fiet.sistema_electivas.dto.ElectivaResponseDTO;
import com.unicauca.fiet.sistema_electivas.model.Electiva;
import java.util.List;

/**
 * Interfaz que define el contrato para la lógica de negocio de las Electivas.
 */
public interface ElectivaService {

    /**
     * HU1.3.1: Crea una nueva electiva en estado BORRADOR.
     * @param dto El objeto Electiva a persistir.
     * @return La electiva guardada con su ID asignado.
     */
    ElectivaResponseDTO crearElectiva(CrearElectivaDTO dto);

    /**
     * HU1.3.2: Actualiza la información de una electiva existente.
     * @param id El ID de la electiva a actualizar.
     * @param dto El objeto Electiva con la nueva información.
     * @return La electiva actualizada.
     */
    Electiva actualizarElectiva(Long id, ActualizarElectivaDTO dto);

    /**
     * HU1.2.3: Desactiva una electiva, cambiando su estado a INACTIVA.
     * @param id El ID de la electiva a desactivar.
     */
    void desactivarElectiva(Long id);

    /**
     * Reactiva una electiva previamente inactiva, cambiando su estado a APROBADA.
     * @param id El ID de la electiva a reactivar.
     */
    void reactivarElectiva(Long id);

    /**
     * HU1.3.5: Aprueba una electiva en estado BORRADOR, cambiándolo a APROBADA.
     * @param id El ID de la electiva a aprobar.
     */
    void aprobarElectiva(Long id);

    /**
     * HU1.3.4: Busca y/o lista las electivas.
     * @param mostrarInactivas Si es true, incluye las electivas inactivas en los resultados.
     * @param query Texto para buscar por código o nombre.
     * @return Una lista de electivas que coinciden con los criterios.
     */
    List<Electiva> findElectivas(boolean mostrarInactivas, String query);
}