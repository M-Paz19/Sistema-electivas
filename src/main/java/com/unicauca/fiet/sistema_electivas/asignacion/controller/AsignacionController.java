package com.unicauca.fiet.sistema_electivas.asignacion.controller;

import com.unicauca.fiet.sistema_electivas.asignacion.service.AsignacionService;

import com.unicauca.fiet.sistema_electivas.asignacion.service.ConsultaAsignacionService;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CambioEstadoValidacionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    private final ConsultaAsignacionService consultaAsignacionService;
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
     * Ejecuta el proceso de asignación masiva de electivas para todos los
     * estudiantes aptos del período académico indicado.
     *
     * <p>Este proceso aplica el algoritmo completo de asignación,
     * estudiante por estudiante, incluyendo asignación directa
     * y recorridos de lista de espera.</p>
     *
     * <p>El período debe encontrarse en estado
     * {@code EN_PROCESO_ASIGNACION} para poder ejecutar este proceso.</p>
     *
     * @param periodoId ID del período académico
     * @return Resumen con el estado final del período y estadísticas del proceso
     */
    @PostMapping("/periodos/{periodoId}/procesar-asignacion")
    public ResponseEntity<CambioEstadoValidacionResponse> procesarAsignacionMasiva(
            @PathVariable Long periodoId
    ) {
        CambioEstadoValidacionResponse respuesta = asignacionService.procesarAsignacionMasiva(periodoId);

        return ResponseEntity.ok(respuesta);
    }

}
