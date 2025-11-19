package com.unicauca.fiet.sistema_electivas.asignacion.controller;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteOrdenamientoResponse;
import com.unicauca.fiet.sistema_electivas.asignacion.service.AsignacionService;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CambioEstadoValidacionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.DatosAcademicoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador encargado de gestionar los procesos relacionados
 * con la asignación de cupos y la validación de estudiantes elegibles
 * para cursar electivas dentro de un período académico.
 *
 * <p>Incluye los endpoints para filtrar estudiantes no elegibles
 * según las reglas del sistema (HU3.1.1).</p>
 */
@RestController
@RequestMapping("/api/asignacion")
@RequiredArgsConstructor
public class AsignacionController {

    private final AsignacionService asignacionService;

    /**
     * HU 3.1.1 – Filtrado de estudiantes no elegibles.
     *
     * <p>Descarta estudiantes que ya cursaron todas las electivas,
     * con el fin de garantizar una asignación justa y transparente.</p>
     *
     * <p>Este proceso solo puede ejecutarse cuando el período
     * está en estado {@code EN_PROCESO_ASIGNACION}.</p>
     *
     * @param periodoId ID del período académico.
     * @return DTO con información sobre el resultado del proceso.
     */
    @PostMapping("/periodos/{periodoId}/filtrar-no-elegibles")
    public ResponseEntity<CambioEstadoValidacionResponse> filtrarNoElegibles(
            @PathVariable Long periodoId) {

        CambioEstadoValidacionResponse response =
                asignacionService.filtrarEstudiantesNoElegibles(periodoId);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener la lista de estudiantes aptos ordenados para el proceso
     * de asignación de electivas.
     *
     * <p>Este endpoint debe usarse únicamente cuando el período académico ya se
     * encuentra en un estado que permite iniciar el algoritmo de asignación
     * (por ejemplo: EN_ORDENAMIENTO_APTOS o EN_PROCESO_ASIGNACION, según tu flujo).</p>
     *
     * <p>Devuelve una lista de estudiantes cuyo estado_aptitud = APTO, ordenados
     * aplicando los criterios oficiales:</p>
     * <ul>
     *   <li>Porcentaje de avance (DESC)</li>
     *   <li>Promedio de carrera (DESC)</li>
     *   <li>Electivas faltantes (ASC)</li>
     * </ul>
     *
     * <p>El resultado es utilizado por el frontend para visualizar el orden exacto
     * que seguirá el algoritmo de asignación.</p>
     *
     * @param periodoId ID del período académico
     * @return Lista ordenada de estudiantes aptos para asignación
     */
    @GetMapping("/periodos/{periodoId}/aptos/ordenados")
    public ResponseEntity<List<EstudianteOrdenamientoResponse>> obtenerAptosOrdenados(
            @PathVariable Long periodoId
    ) {
        List<EstudianteOrdenamientoResponse> respuesta = asignacionService.obtenerAptosOrdenados(periodoId);

        return ResponseEntity.ok(respuesta);
    }
}
