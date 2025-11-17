package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.DatosAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.ValidacionNiveladoResponseDTO;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.VerificacionNiveladoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ValidacionNiveladosService {

    /**
     * Genera un reporte visual de la nivelación de los estudiantes.
     * 
     * <p>Lee el archivo Excel de historia académica, compara con la malla del programa
     * hasta el semestre actual del estudiante y genera una vista que muestra
     * el avance y posibles brechas.</p>
     *
     * @param idDatosAcademicos ID del estudiante que es posible nivelado
     * @param archivoExcel Archivo Excel con la historia académica de los estudiantes
     * @return DTO con la información lista para visualización en frontend
     */
    VerificacionNiveladoDTO generarReporteNivelado(MultipartFile archivoExcel, Long idDatosAcademicos);

    /**
     * Registra la decisión final del administrador sobre si un estudiante
     * cumple o no con el nivel de avance esperado.
     *
     * @param idDatosAcademicos ID del los datos academicos asociados al estudiante a tomar la decision
     * @param niveladoFinal true si el estudiante se considera nivelado, false en caso contrario
     * @return DTO con el resultado de la decisión
     */
    DatosAcademicoResponse registrarDecisionFinal(Long idDatosAcademicos, boolean niveladoFinal);
    /**
     * HU 2.2.1.1 / 2.2.1.2: Preselección automática de posibles nivelados.
     *
     * <p>Identifica a los estudiantes que cumplen los criterios mínimos de
     * nivelación definidos en las reglas del plan de estudios (minCreditosAprobados
     * y maxPeriodosMatriculados). Solo puede ejecutarse cuando el período está en
     * estado <b>PROCESO_CARGA_SIMCA</b>.</p>
     *
     * <p>El proceso realiza:</p>
     * <ul>
     *     <li>Validación del estado del período.</li>
     *     <li>Obtención de todos los registros de datos académicos cargados.</li>
     *     <li>Evaluación de cada estudiante frente a las reglas de su plan.</li>
     *     <li>Marcación como <b>POSIBLE_NIVELADO</b> si cumple al menos una regla.</li>
     *     <li>Transición del período a <b>PROCESO_REVISION_POTENCIALES_NIVELADOS</b>.</li>
     *     <li>Retorno del listado de estudiantes preseleccionados.</li>
     * </ul>
     *
     * @param idPeriodo ID del período académico.
     * @return lista de estudiantes candidatos a nivelación representados como {@link DatosAcademicoResponse}.
     *
     * @throws ResourceNotFoundException si el período no existe.
     * @throws InvalidStateException si el período no está en estado PROCESO_CARGA_SIMCA.
     */
    List<DatosAcademicoResponse> preseleccionarNivelados(Long idPeriodo);
}
