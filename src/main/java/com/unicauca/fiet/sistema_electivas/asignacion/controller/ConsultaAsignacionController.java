package com.unicauca.fiet.sistema_electivas.asignacion.controller;

import com.unicauca.fiet.sistema_electivas.asignacion.dto.EstudianteOrdenamientoResponse;
import com.unicauca.fiet.sistema_electivas.asignacion.service.ConsultaAsignacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador encargado de exponer endpoints de consulta
 * relacionados con estudiantes aptos y su ordenamiento
 * para el proceso de asignación de electivas.
 *
 * <p>Este controlador no modifica estados ni realiza asignaciones,
 * solo permite consultar la información existente.</p>
 */
@RestController
@RequestMapping("/api/consulta-asignacion")
@RequiredArgsConstructor
public class ConsultaAsignacionController {

    private final ConsultaAsignacionService consultaAsignacionService;

    /**
     * Obtiene la lista de estudiantes aptos ordenados para un período académico.
     *
     * <p>Este endpoint devuelve únicamente información de consulta, sin modificar
     * ningún estado del sistema.</p>
     *
     * <p>Los estudiantes se ordenan aplicando los criterios oficiales:</p>
     * <ul>
     *   <li>Porcentaje de avance (DESC)</li>
     *   <li>Promedio de carrera (DESC)</li>
     *   <li>Electivas faltantes (ASC)</li>
     * </ul>
     *
     * @param periodoId ID del período académico
     * @return Lista de estudiantes aptos ordenados para asignación
     */
    @GetMapping("/periodos/{periodoId}/aptos/ordenados")
    public ResponseEntity<List<EstudianteOrdenamientoResponse>> obtenerAptosOrdenados(
            @PathVariable Long periodoId
    ) {
        List<EstudianteOrdenamientoResponse> estudiantes =
                consultaAsignacionService.obtenerAptosOrdenados(periodoId);

        return ResponseEntity.ok(estudiantes);
    }

    // Aquí puedes agregar más endpoints de consulta en el futuro
    // Ej: obtener asignaciones de un estudiante específico, estadísticas, etc.
}
