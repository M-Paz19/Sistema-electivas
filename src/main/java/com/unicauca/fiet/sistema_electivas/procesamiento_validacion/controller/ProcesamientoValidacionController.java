package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.controller;

import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service.ProcesamientoValidacionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la consulta y gestión de respuestas de formularios
 * asociadas a los períodos académicos.
 *
 * <p>Permite obtener las respuestas importadas desde Google Forms
 * como paso previo al procesamiento o validación académica.</p>
 */
@RestController
@RequestMapping("/api/procesamiento")
@RequiredArgsConstructor
public class ProcesamientoValidacionController {

    private final ProcesamientoValidacionServiceImpl procesamientoService;


    /**
     * Obtiene todas las respuestas asociadas a un período académico específico.
     *
     * @param periodoId ID del período académico
     * @return Lista de respuestas del formulario vinculadas a ese período.
     */
    @GetMapping("/periodos/{periodoId}/respuestas")
    public ResponseEntity<List<RespuestaFormularioResponse>> listarRespuestasPorPeriodo(@PathVariable Long periodoId) {
        List<RespuestaFormularioResponse> respuestas = procesamientoService.obtenerRespuestasPorPeriodo(periodoId);
        return ResponseEntity.ok(respuestas);
    }

}
