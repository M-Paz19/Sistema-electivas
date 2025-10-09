package com.unicauca.fiet.sistema_electivas.periodo_academico.controller;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.AgregarElectivaOfertadaDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.CrearPeriodoAcademicoDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.ElectivaOfertadaResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.PeriodoAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.service.PeriodoAcademicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/periodos-academicos")
@RequiredArgsConstructor
public class PeriodoAcademicoController {

    private final PeriodoAcademicoService periodoService;


    /**
     * Crea un nuevo período académico en el sistema.
     *
     * <p>Valida que el semestre sea único, el formato sea correcto y las fechas sean coherentes.
     * El nuevo período se crea en estado {@link EstadoPeriodoAcademico#CONFIGURACION}.</p>
     *
     * @param dto DTO {@link CrearPeriodoAcademicoDTO} con los datos del período a crear
     * @return {@link PeriodoAcademicoResponse} con la información del período creado
     * @throws DuplicateResourceException si ya existe un período con el mismo semestre
     * @throws IllegalArgumentException si el formato del semestre o las fechas son inválidos
     */

    @PostMapping
    public ResponseEntity<PeriodoAcademicoResponse> crearPeriodo(@Validated @RequestBody CrearPeriodoAcademicoDTO dto) {
        PeriodoAcademicoResponse creado = periodoService.crearPeriodo(dto);
        return ResponseEntity.ok(creado);
    }

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
    @PostMapping("/periodos/{periodoId}/electivas")
    public ResponseEntity<ElectivaOfertadaResponse> agregarElectivaOfertada(
            @PathVariable Long periodoId,
            @Validated @RequestBody AgregarElectivaOfertadaDTO dto) {

        ElectivaOfertadaResponse response = periodoService.agregarElectivaOfertada(periodoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene la lista de períodos académicos, con opción de filtrar por semestre o estado.
     *
     * <p>La lista se ordena de más reciente a más antiguo por semestre.</p>
     *
     * @param semestreTexto (opcional) texto parcial para buscar por semestre
     * @param estado (opcional) estado para filtrar
     * @return Lista de períodos académicos resumidos en {@link PeriodoAcademicoResponse}
     */
    @GetMapping
    public ResponseEntity<List<PeriodoAcademicoResponse>> listarPeriodos(
            @RequestParam(required = false) String semestreTexto,
            @RequestParam(required = false) EstadoPeriodoAcademico estado) {

        List<PeriodoAcademicoResponse> periodos = periodoService.listarPeriodos(semestreTexto, estado);
        return ResponseEntity.ok(periodos);
    }

    /**
     * Obtiene todas las electivas ofertadas de un período académico específico.
     *
     * @param periodoId ID del período académico
     * @return Lista de electivas ofertadas asociadas a ese período
     */
    @GetMapping("/{periodoId}/electivas")
    public ResponseEntity<List<ElectivaOfertadaResponse>> listarElectivasPorPeriodo(@PathVariable Long periodoId) {
        List<ElectivaOfertadaResponse> electivas = periodoService.listarElectivasPorPeriodo(periodoId);
        return ResponseEntity.ok(electivas);
    }

}
