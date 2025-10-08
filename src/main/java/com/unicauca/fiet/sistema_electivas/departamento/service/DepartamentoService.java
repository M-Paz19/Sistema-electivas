package com.unicauca.fiet.sistema_electivas.departamento.service;

import com.unicauca.fiet.sistema_electivas.departamento.dto.DepartamentoRequestDTO;
import com.unicauca.fiet.sistema_electivas.departamento.dto.DepartamentoResponse;
import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;
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
    DepartamentoResponse crearDepartamento(DepartamentoRequestDTO departamento);

    /**
     * HU1.2.3: Actualiza la información de un departamento existente.
     * @param id El ID del departamento a actualizar.
     * @param dto El objeto Departamento con la nueva información.
     * @return El departamento actualizado.
     */
    DepartamentoResponse actualizarDepartamento(Long id, DepartamentoRequestDTO dto);

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
    List<DepartamentoResponse> findDepartamentos(String filtroEstado, String query);
    /**
     * Buscar un departamento por su id.
     * @param id el departamento con el id asignado.
     * @return el departamento correspondiente al id.
     */
    DepartamentoResponse buscarPorId(Long id);
}
