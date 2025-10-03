package com.unicauca.fiet.sistema_electivas.service;

import com.unicauca.fiet.sistema_electivas.model.Departamento;
import java.util.List;

/**
 * Interfaz que define el contrato para la lógica de negocio de los Departamentos.
 */
public interface DepartamentoService {

    /**
     * HU1.2.1: Crea un nuevo departamento en el sistema.
     * @param departamento El objeto Departamento a persistir.
     * @return El departamento guardado con su ID asignado.
     */
    Departamento crearDepartamento(Departamento departamento);

    /**
     * HU1.2.3: Actualiza la información de un departamento existente.
     * @param id El ID del departamento a actualizar.
     * @param deptoDetails El objeto Departamento con la nueva información.
     * @return El departamento actualizado.
     */
    Departamento actualizarDepartamento(Long id, Departamento deptoDetails);

    /**
     * HU1.2.3: Deshabilita un departamento, cambiando su estado a INACTIVO.
     * @param id El ID del departamento a deshabilitar.
     */
    void deshabilitarDepartamento(Long id);

    /**
     * HU1.2.4: Busca y/o lista los departamentos según un estado y una consulta.
     * @param filtroEstado Filtra por estado ("ACTIVO", "INACTIVO", "TODOS").
     * @param query Texto para buscar por código o nombre.
     * @return Una lista de departamentos que coinciden con los criterios.
     */
    List<Departamento> findDepartamentos(String filtroEstado, String query);
}
