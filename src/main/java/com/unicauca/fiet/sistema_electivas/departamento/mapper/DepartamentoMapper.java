package com.unicauca.fiet.sistema_electivas.departamento.mapper;

import com.unicauca.fiet.sistema_electivas.departamento.dto.*;
import com.unicauca.fiet.sistema_electivas.departamento.model.Departamento;

public class DepartamentoMapper {

    public static DepartamentoResponse toResponse(Departamento depto) {
        return new DepartamentoResponse(
                depto.getId(),
                depto.getCodigo(),
                depto.getNombre(),
                depto.getDescripcion(),
                depto.getEstado().name()
        );
    }

    public static Departamento toEntity(DepartamentoRequestDTO dto) {
        Departamento depto = new Departamento();
        depto.setCodigo(dto.getCodigo());
        depto.setNombre(dto.getNombre());
        depto.setDescripcion(dto.getDescripcion());
        return depto;
    }
}
