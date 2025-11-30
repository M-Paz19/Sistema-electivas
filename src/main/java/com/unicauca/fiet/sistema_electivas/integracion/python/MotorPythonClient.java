package com.unicauca.fiet.sistema_electivas.integracion.python;

import com.unicauca.fiet.sistema_electivas.reporte.dto.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MotorPythonClient {

    private final RestTemplate restTemplate;

    public MotorPythonClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envía al microservicio Python la lista de asignaciones individuales de estudiantes
     * para calcular la distribución global de electivas asignadas.
     *
     * HU 4 – Distribución de asignaciones.
     *
     * @param asignaciones lista de asignaciones mínimas, una entrada por estudiante
     * @return la distribución agregada de electivas asignadas (JSON procesado por Python)
     */
    public DistribucionAsignacionesResponse calcularDistribucion(
            List<AsignacionElectivaMinDto> asignaciones
    ) {
        String url = "http://localhost:8000/estadisticas/distribucion";

        Map<String, Object> request = new HashMap<>();
        request.put("asignaciones", asignaciones);

        return restTemplate.postForObject(
                url,
                request,
                DistribucionAsignacionesResponse.class
        );
    }

    /**
     * Envía al microservicio Python la lista de asignaciones agrupadas por programa académico
     * para calcular cuántas electivas fueron asignadas dentro de cada programa en un semestre.
     * HU 4.1 – Distribución de electivas asignadas por programa.
     *
     * @param asignaciones lista de asignaciones clasificadas por programa académico
     * @param semestre semestre a evaluar (ej. "2025-1")
     * @return distribución por programa procesada por el motor estadístico en Python
     */
    public DistribucionAsignacionesPorProgramaResponse calcularDistribucionPorPrograma(
            List<AsignacionElectivaProgramaDto> asignaciones,
            String semestre
    ) {
        String url = "http://localhost:8000/estadisticas/distribucion-por-programa?semestre=" + semestre;

        Map<String, Object> request = new HashMap<>();
        request.put("asignaciones", asignaciones);

        return restTemplate.postForObject(
                url,
                request,
                DistribucionAsignacionesPorProgramaResponse.class
        );
    }

    /**
     * Solicita al microservicio Python la generación del reporte Excel
     * que incluye:
     *  - distribución general de asignaciones,
     *  - distribución por programa académico,
     *  - resumen del procesamiento del período.
     *  – Generación de reporte estadístico en Excel.
     *
     * @param distribucionEstudiantes distribución global de asignaciones
     * @param distribucionProgramas distribución agrupada por programas académicos
     * @param resumenPeriodo resumen de estados del procesamiento del período
     * @return archivo Excel en formato binario (byte[])
     */
    public byte[] generarReporteDistribucionExcel(
            DistribucionAsignacionesResponse distribucionEstudiantes,
            DistribucionAsignacionesPorProgramaResponse distribucionProgramas,
            ResumenProcesamientoPeriodoResponse resumenPeriodo) {

        String url = "http://localhost:8000/estadisticas/reporte-distribucion";

        Map<String, Object> request = new HashMap<>();
        request.put("distribucionEstudiantes", distribucionEstudiantes);
        request.put("distribucionProgramas", distribucionProgramas);
        request.put("resumenPeriodo", resumenPeriodo);

        return restTemplate.postForObject(url, request, byte[].class);
    }

    /**
     * Envía al microservicio Python la lista de solicitudes de estudiantes
     * (opciones registradas en el formulario) para calcular la popularidad de cada electiva.
     *  – Popularidad de electivas según preferencias del estudiantado.
     * El microservicio Python:
     *  - agrupa por electiva,
     *  - cuenta cuántos estudiantes la marcaron como opción 1, 2, 3, etc.,
     *  - genera un total por electiva,
     *  - ordena de mayor a menor popularidad.
     *
     * @param solicitudes lista de solicitudes generadas desde las respuestas válidas del sistema
     * @return respuesta con el semestre y el ranking de popularidad procesado por Python
     */
    public PopularidadElectivasResponse calcularPopularidad(
            List<PopularidadRequestDto> solicitudes
    ) {
        String url = "http://localhost:8000/estadisticas/popularidad";

        Map<String, Object> request = new HashMap<>();
        request.put("solicitudes", solicitudes);

        return restTemplate.postForObject(
                url,
                request,
                PopularidadElectivasResponse.class
        );
    }


    /**
     * Solicita al microservicio Python la generación del reporte Excel de popularidad
     * que incluye:
     *  - popularidad de electivas considerando únicamente respuestas válidas/aptas,
     *  - popularidad de electivas incluyendo respuestas descartadas (NO_CUMPLE, DESCARTADO, etc.).
     *
     * <p>HU 4.3 – Generación del reporte comparativo de popularidad de electivas.</p>
     *
     * @param popularidadAptos popularidad calculada solo con respuestas válidas
     * @param popularidadDescartados popularidad incluyendo estudiantes descartados o no aptos
     * @return archivo Excel en formato binario (byte[])
     */
    public byte[] generarReportePopularidadExcel(
            PopularidadElectivasResponse popularidadAptos,
            PopularidadElectivasResponse popularidadDescartados) {

        String url = "http://localhost:8000/estadisticas/reporte-popularidad";

        Map<String, Object> request = new HashMap<>();
        request.put("popularidadAptos", popularidadAptos);
        request.put("popularidadDescartados", popularidadDescartados);

        return restTemplate.postForObject(url, request, byte[].class);
    }

}