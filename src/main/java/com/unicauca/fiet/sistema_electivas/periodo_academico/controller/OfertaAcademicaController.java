package com.unicauca.fiet.sistema_electivas.periodo_academico.controller;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.EditarCuposDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.OfertaRequestDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.OfertaResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.service.OfertaAcademicaService;
import com.unicauca.fiet.sistema_electivas.periodo_academico.service.PeriodoAcademicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OfertaAcademicaController {
    private final OfertaAcademicaService ofertaService;

    /**
     * Agrega una electiva aprobada a la oferta académica de un período en estado CONFIGURACION.
     *
     * <p>Valida que el período exista y sea editable, que la electiva esté aprobada,
     * que no haya duplicados, y ajusta los cupos por programa.</p>
     *
     * @param periodoId ID del período académico
     * @param dto DTO con la electiva y los cupos por programa (opcional)
     * @return Detalle de la electiva ofertada creada
     * @throws ResourceNotFoundException si el período o la electiva no existen
     * @throws InvalidStateException si el período no está en estado CONFIGURACION
     * @throws BusinessException si la electiva no está aprobada o ya existe en este periodo
     * @throws IllegalArgumentException si los cupos son inválidos
     */
    @PostMapping("/periodos/{periodoId}/ofertas")
    public ResponseEntity<OfertaResponse> agregarElectivaOfertada(
            @PathVariable Long periodoId,
            @Validated @RequestBody OfertaRequestDTO dto) {

        OfertaResponse response = ofertaService.agregarElectivaOfertada(periodoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/periodos/{periodoId}/electivas-ofertadas/{ofertadaId}/cupos
     * Editar los cupos por programa de una electiva ofertada
     */
    @PutMapping("/ofertas/{ofertaId}/cupos")
    public ResponseEntity<OfertaResponse> editarCupos(
            @PathVariable Long ofertaId,
            @Validated @RequestBody EditarCuposDTO dto) {

        OfertaResponse actualizada = ofertaService.editarCupos(ofertaId, dto);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * DELETE /api/periodos/{periodoId}/electivas-ofertadas/{ofertadaId}
     * Eliminar una electiva ofertada de un periodo
     */
    @DeleteMapping("/ofertas/{ofertaId}")
    public ResponseEntity<Map<String, String>> eliminarElectivaOfertada(
            @PathVariable Long ofertaId) {
        ofertaService.eliminarElectivaOfertada(ofertaId);
        return ResponseEntity.ok(Map.of("mensaje", "Electiva ofertada eliminada correctamente"));
    }
    /**
     * Obtiene todas las electivas ofertadas de un período académico específico.
     *
     * @param periodoId ID del período académico
     * @return Lista de electivas ofertadas asociadas a ese período
     */
    @GetMapping("/periodos/{periodoId}/ofertas")
    public ResponseEntity<List<OfertaResponse>> listarElectivasPorPeriodo(@PathVariable Long periodoId) {
        List<OfertaResponse> electivas = ofertaService.listarElectivasPorPeriodo(periodoId);
        return ResponseEntity.ok(electivas);
    }
}
