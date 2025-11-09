package com.unicauca.fiet.sistema_electivas.periodo_academico.controller;


import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.DuplicateResourceException;

import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.*;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;

import com.unicauca.fiet.sistema_electivas.periodo_academico.service.PeriodoAcademicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * Abre un período académico existente, cambiando su estado de CONFIGURACION a ABIERTO.
     *
     * <p>Solo puede abrirse si cumple las condiciones definidas en {@link PeriodoAcademicoService#abrirPeriodo}.
     * Si la fecha actual es anterior a la fecha de apertura, se requiere {@code forzarApertura = true}.</p>
     *
     * @param periodoId ID del período académico a abrir
     * @param request Datos de apertura, incluyendo el indicador de forzar apertura y el número de opciones del formulario
     * @return {@link CambioEstadoResponse} con el nuevo estado del período y mensaje de confirmación
     * @throws ResourceNotFoundException si no existe el período con el ID indicado
     * @throws BusinessException si no cumple las condiciones para ser abierto
     */
    @PostMapping("/{periodoId}/abrir")
    public ResponseEntity<CambioEstadoResponse> abrirPeriodo(
            @PathVariable Long periodoId,
            @Valid @RequestBody AbrirPeriodoRequest request) {
        CambioEstadoResponse response = periodoService.abrirPeriodo(
                periodoId,
                request.isForzarApertura(),
                request.getNumeroOpcionesFormulario()
        );
        return ResponseEntity.ok(response);
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
     * Cierra el formulario de preinscripción asociado a un período académico.
     *
     * <p>Este proceso marca el fin de la etapa de recolección de respuestas y cambia el estado
     * del período a {@link EstadoPeriodoAcademico#CERRADO_FORMULARIO}.
     * El sistema intenta obtener automáticamente las respuestas desde Google Forms,
     * usando la URL configurada para ese período.</p>
     *
     * @param periodoId ID del período académico a cerrar
     * @return Mensaje de confirmación con el nuevo estado del período
     * @throws InvalidStateException si el período no está en estado ABIERTO o no tiene formulario asociado
     * @throws RuntimeException si ocurre un error durante el cierre o la obtención de respuestas
     */
    @PostMapping("/{periodoId}/cerrar-formulario")
    public ResponseEntity<CambioEstadoResponse> cerrarFormulario(@PathVariable Long periodoId) {
        CambioEstadoResponse actualizado = periodoService.cerrarFormulario(periodoId);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Endpoint para cargar manualmente las respuestas del formulario de preinscripción.
     *
     * <p>Permite a un administrador académico subir un archivo (Excel o CSV) con las respuestas
     * cuando la obtención automática desde Google Forms no fue posible o se desea una carga manual.</p>
     *
     * @param periodoId ID del período académico al que corresponden las respuestas.
     * @param file Archivo con las respuestas a importar.
     * @return Respuesta HTTP 200 con el objeto {@link CambioEstadoResponse} en caso de éxito.
     */
    @PostMapping("/{periodoId}/cargar-respuestas")
    public ResponseEntity<CambioEstadoResponse> cargarRespuestasManual(
            @PathVariable Long periodoId,
            @RequestParam("file") MultipartFile file) {

        CambioEstadoResponse response = periodoService.cargarRespuestasManual(periodoId, file);
        return ResponseEntity.ok(response);
    }

}
