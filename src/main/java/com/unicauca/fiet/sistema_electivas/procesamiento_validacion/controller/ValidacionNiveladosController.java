package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.controller;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.ValidacionNiveladoResponseDTO;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.VerificacionNiveladoDTO;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service.ValidacionNiveladosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador para el módulo de validación de nivelados.
 *
 * <p>Expone los endpoints para generar el reporte de comparación entre la malla académica
 * y la historia académica de un estudiante, así como para registrar la decisión final
 * sobre si el estudiante cumple o no con los requisitos de nivelación.</p>
 */
@RestController
@RequestMapping("/api/validacion-nivelados")
@RequiredArgsConstructor
public class ValidacionNiveladosController {

    private final ValidacionNiveladosService validacionNiveladosService;

    /**
     * Genera un reporte visual de nivelación para un estudiante.
     *
     * <p>Lee el archivo Excel de historia académica, compara con la malla del programa
     * hasta el semestre actual del estudiante y devuelve un informe con las materias
     * aprobadas, reprobadas o pendientes.</p>
     *
     * @param idDatosAcademicos ID del estudiante (registro de datos académicos)
     * @param archivoExcel archivo Excel con el historial académico del estudiante
     * @return {@link VerificacionNiveladoDTO} con el resultado de la comparación
     */
    @PostMapping("/reporte/{idDatosAcademicos}")
    public ResponseEntity<VerificacionNiveladoDTO> generarReporteNivelado(
            @PathVariable Long idDatosAcademicos,
            @RequestParam("archivo") MultipartFile archivoExcel) {

        VerificacionNiveladoDTO resultado = validacionNiveladosService
                .generarReporteNivelado(archivoExcel, idDatosAcademicos);

        return ResponseEntity.ok(resultado);
    }

    /**
     * Registra la decisión final del administrador sobre la nivelación del estudiante.
     *
     * <p>Este endpoint permite definir si el estudiante se considera nivelado o no,
     * en función del reporte visual o criterios adicionales del operador.</p>
     *
     * @param idDatosAcademicos ID del estudiante (registro de datos académicos)
     * @param nivelado indica si el estudiante se considera finalmente nivelado
     * @return {@link ValidacionNiveladoResponseDTO} con el resultado de la decisión
     */
    @PostMapping("/decision-final/{idDatosAcademicos}")
    public ResponseEntity<ValidacionNiveladoResponseDTO> registrarDecisionFinal(
            @PathVariable Long idDatosAcademicos,
            @RequestParam("nivelado") boolean nivelado) {

        ValidacionNiveladoResponseDTO resultado = validacionNiveladosService
                .registrarDecisionFinal(idDatosAcademicos, nivelado);

        return ResponseEntity.ok(resultado);
    }
}
