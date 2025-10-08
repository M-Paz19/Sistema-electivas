package com.unicauca.fiet.sistema_electivas.periodo_academico.controller;

import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.AgregarElectivaOfertadaDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.CrearPeriodoAcademicoDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.ElectivaOfertadaResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.PeriodoAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.service.PeriodoAcademicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/periodos-academicos")
@RequiredArgsConstructor
public class PeriodoAcademicoController {

    private final PeriodoAcademicoService periodoService;


    @PostMapping
    public ResponseEntity<PeriodoAcademicoResponse> crearPeriodo(@Validated @RequestBody CrearPeriodoAcademicoDTO dto) {
        PeriodoAcademicoResponse creado = periodoService.crearPeriodo(dto);
        return ResponseEntity.ok(creado);
    }

    /**
     * Agrega una electiva aprobada a la oferta académica de un período en configuración.
     *
     * @param periodoId ID del período académico en configuración
     * @param dto       Datos de la electiva a ofertar y los cupos por programa (opcional)
     * @return Detalle de la electiva ofertada creada
     */
    @PostMapping("/periodos/{periodoId}/electivas")
    public ResponseEntity<ElectivaOfertadaResponse> agregarElectivaOfertada(
            @PathVariable Long periodoId,
            @Validated @RequestBody AgregarElectivaOfertadaDTO dto) {

        ElectivaOfertadaResponse response = periodoService.agregarElectivaOfertada(periodoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
