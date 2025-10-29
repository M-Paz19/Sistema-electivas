package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.RespuestaFormularioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcesamientoValidacionServiceImpl implements  ProcesamientoValidacionService {

    private final RespuestasFormularioRepository respuestasRepository;


    /**
     * {@inheritDoc}
     */
    public List<RespuestaFormularioResponse> obtenerRespuestasPorPeriodo(Long periodoId) {
        List<RespuestasFormulario> entidades = respuestasRepository.findByPeriodoId(periodoId);
        return RespuestaFormularioMapper.toResponseList(entidades);
    }

}
