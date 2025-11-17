package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.plan_estudio.dto.ReglaNivelacion;
import com.unicauca.fiet.sistema_electivas.plan_estudio.enums.TipoMateria;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanEstudio;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanMateria;
import com.unicauca.fiet.sistema_electivas.plan_estudio.repository.PlanMateriaRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.*;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.enums.EstadoAptitud;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.DatosAcademicoMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.VerificacionNiveladoMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.model.DatosAcademico;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.repository.DatosAcademicoRepository;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ValidacionNiveladosServiceImpl implements ValidacionNiveladosService {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DatosAcademicoRepository datosAcademicoRepository;
    @Autowired
    private PlanMateriaRepository planMateriaRepository;
    @Autowired
    private ExcelHistorialAcademicoService  excelHistorialAcademicoService;
    @Autowired
    private PeriodoAcademicoRepository periodoRepository;
    @Autowired
    private DatosAcademicoMapper datosAcademicoMapper;
    @Autowired
    private VerificacionNiveladoMapper verificacionNiveladoMapper;
    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public VerificacionNiveladoDTO generarReporteNivelado(MultipartFile archivoExcel, Long idDatosAcademicos) {
        // 1. Buscar datos académicos
        DatosAcademico datos = datosAcademicoRepository.findById(idDatosAcademicos)
                .orElseThrow(() -> new ResourceNotFoundException("No existe registro académico para el ID: " + idDatosAcademicos));

        // 2. Validar estado actual solicitud estudiante
        if (datos.getEstadoAptitud()!=EstadoAptitud.POSIBLE_NIVELADO) {
            throw new InvalidStateException("El estudiante no es un 'POSIBLE_NIVELADO'.");
        }
        // Validar que el período esté exactamente en el estado PROCESO_CARGA_SIMCA
        if (datos.getRespuesta().getPeriodo().getEstado() != EstadoPeriodoAcademico.PROCESO_REVISION_POTENCIALES_NIVELADOS) {
            throw new InvalidStateException("Solo se pueden verificar la nivelación de un estudiante despues de preseleccionar los posibles nivelados.");
        }
        // 3. Obtener número de semestres cursados
        int semestres = determinarSemestreVerificacion(datos);
        if (semestres==-1) {
            throw new BusinessException("El estudiante no cumple con ninguna de las reglas de nivelación del su plan.");
        }

        // 4. Traer materias del plan de estudios hasta ese semestre
        List<PlanMateria> planMaterias = planMateriaRepository
                .findByPlanEstudios_IdAndSemestreLessThanEqualOrderBySemestreAscNombreAsc(datos.getPlanEstudios().getId(), semestres);

        // 5. Leer materias aprobadas desde el Excel
        List<MateriaVistaExcel> materiasExcel = excelHistorialAcademicoService.parsearHistorialAcademico(archivoExcel);

        // 6. Comparar materias esperadas vs. materias aprobadas
        List<MateriaComparadaDTO> comparacion = compararMaterias(planMaterias, materiasExcel);

        // 7. Calcular porcentaje de avance (materias aprobadas / materias esperadas)
        boolean todasAprobadas = comparacion.stream().allMatch(MateriaComparadaDTO::isAprobada);
        BigDecimal porcentaje = BigDecimal.valueOf(
                (double) comparacion.stream().filter(MateriaComparadaDTO::isAprobada).count() * 100 / planMaterias.size()
        ).setScale(2, RoundingMode.HALF_UP);

        // 9. Construir DTO de salida
        return verificacionNiveladoMapper.toDTO(
                datos,
                comparacion,
                todasAprobadas,
                semestres
        );
    }
    /**
     * Determina hasta qué semestre deben verificarse las materias para el cálculo de nivelación.
     *
     * <p>El método evalúa las <b>reglas de nivelación</b> definidas en el plan de estudios,
     * las cuales contienen los valores mínimos de créditos aprobados y máximos de períodos
     * matriculados permitidos para cada semestre. A partir de estas reglas, se selecciona
     * el semestre más alto que el estudiante cumple.</p>
     *
     * <p>Si el estudiante no cumple ninguna regla, se usa un fallback:
     * <b>periodosMatriculados - 1</b> (mínimo 1).</p>
     *
     * @param datos Información académica del estudiante.
     * @return número del semestre hasta el cual deben verificarse materias (semestre - 1).
     */
    private int determinarSemestreVerificacion(DatosAcademico datos) {

        PlanEstudio plan = datos.getPlanEstudios();

        if (plan.getReglasNivelacion() == null || plan.getReglasNivelacion().isEmpty()) {
            // Si un plan no tiene reglas, usar fallback
            return Math.max(datos.getPeriodosMatriculados() - 1, 1);
        }

        // Convertimos el JSON a un MAP tipado
        Map<String, ReglaNivelacion> reglas = objectMapper.convertValue(
                plan.getReglasNivelacion(),
                new TypeReference<Map<String, ReglaNivelacion>>() {}
        );

        int creditos = datos.getCreditosAprobados();
        int periodos = datos.getPeriodosMatriculados();

        int semestreSeleccionado = 0;

        for (Map.Entry<String, ReglaNivelacion> entry : reglas.entrySet()) {

            String nombreSemestre = entry.getKey();     // Ej: "Octavo"
            ReglaNivelacion regla = entry.getValue();   // Tipado 100%

            int numeroSemestre = convertirNombreSemestreANumero(nombreSemestre);

            if (creditos >= regla.getMinCreditosAprobados()
                    && periodos <= regla.getMaxPeriodosMatriculados()) {

                semestreSeleccionado = Math.max(semestreSeleccionado, numeroSemestre);
            }
        }

        // Si no cumple ninguna regla, por fallback usar semestres -1
        if (semestreSeleccionado == 0) {
            return Math.max(periodos - 1, 1);
        }

        // Debo verificar materias hasta semestreAnterior (nivelado en 8 → verificar 1..7)
        return semestreSeleccionado - 1;
    }
    /**
     * Convierte una representación textual de un semestre (por ejemplo "Octavo")
     * a su número correspondiente.
     *
     * <p>Este método es necesario debido a que las reglas de nivelación pueden
     * almacenar las claves como nombres en español y no como números.</p>
     *
     * @param nombre nombre del semestre en formato textual.
     * @return número entero equivalente al semestre.
     * @throws BusinessException si el nombre no coincide con ninguno reconocido.
     */
    private int convertirNombreSemestreANumero(String nombre) {
        switch (nombre.toLowerCase()) {
            case "primero": return 1;
            case "segundo": return 2;
            case "tercero": return 3;
            case "cuarto": return 4;
            case "quinto": return 5;
            case "sexto": return 6;
            case "séptimo":
            case "septimo": return 7;
            case "octavo": return 8;
            case "noveno": return 9;
            case "décimo":
            case "decimo": return 10;
            default: throw new BusinessException(
                    "Nombre de semestre no reconocido: " + nombre
            );
        }
    }
    /**
     * Compara las materias del plan de estudios con las materias vistas en el historial académico.
     *
     * <p>La comparación determina si cada materia requerida por el plan:
     * <ul>
     *     <li>Fue aprobada</li>
     *     <li>Está pendiente</li>
     *     <li>Fue reprobada</li>
     *     <li>Corresponde a grupos especiales como <b>Fish</b> o <b>Electivas</b></li>
     * </ul>
     * El proceso permite validar equivalencias basadas en nombre, cantidad aprobada
     * y reglas especiales para materias electivas.</p>
     *
     * @param plan materias esperadas según el plan de estudios.
     * @param materiasVistas materias extraídas del Excel cargado por el estudiante.
     * @return lista de DTOs indicando el estado de cada materia comparada.
     */
    private List<MateriaComparadaDTO> compararMaterias(List<PlanMateria> plan, List<MateriaVistaExcel> materiasVistas) {
        List<MateriaComparadaDTO> resultado = new ArrayList<>();
        // --- Agrupar materias vistas ---
        List<MateriaVistaExcel> fishAprobadas = materiasVistas.stream()
                .filter(m -> normalizar(m.getNombre()).contains("fish"))
                .filter(this::esAprobada)
                .toList();

        List<MateriaVistaExcel> electivasAprobadas = materiasVistas.stream()
                .filter(m -> normalizar(m.getNombre()).contains("electiva")
                        && !normalizar(m.getNombre()).contains("fish"))
                .filter(this::esAprobada)
                .toList();

        int restantesFish = fishAprobadas.size();
        int restantesElectivas = electivasAprobadas.size();

        for (PlanMateria materiaPlan : plan) {
            MateriaComparadaDTO dto = new MateriaComparadaDTO();
            dto.setNombre(materiaPlan.getNombre());
            dto.setSemestre(materiaPlan.getSemestre());
            dto.setObligatoria(materiaPlan.getTipo() == TipoMateria.OBLIGATORIA || materiaPlan.getTipo() == TipoMateria.ELECTIVA);

            String nombrePlanNorm = normalizar(materiaPlan.getNombre());

            // --- Caso Fish ---
            if (nombrePlanNorm.contains("fish")) {
                if (restantesFish > 0) {
                    dto.setAprobada(true);
                    dto.setObservacion("Aprobada (Fish)");
                    restantesFish--; // consumir una Fish aprobada
                } else {
                    dto.setAprobada(false);
                    dto.setObservacion("Pendiente cursar (Fish)");
                }
                resultado.add(dto);
                continue;
            }

            // --- Caso Electivas generales ---
            if (nombrePlanNorm.contains("electiva") && !nombrePlanNorm.contains("fish")) {
                if (restantesElectivas > 0) {
                    dto.setAprobada(true);
                    dto.setObservacion("Aprobada (Electiva)");
                    restantesElectivas--; // consumir una Electiva aprobada
                } else {
                    dto.setAprobada(false);
                    dto.setObservacion("Pendiente cursar (Electiva)");
                }
                resultado.add(dto);
                continue;
            }

            // --- Caso materias normales ---
            List<MateriaVistaExcel> coincidencias = materiasVistas.stream()
                    .filter(m -> normalizar(m.getNombre()).contains(nombrePlanNorm)
                            || nombrePlanNorm.contains(normalizar(m.getNombre())))
                    .toList();

            if (coincidencias.isEmpty()) {
                dto.setAprobada(false);
                dto.setObservacion("No cursada");
            } else {
                boolean aprobada = coincidencias.stream().anyMatch(this::esAprobada);
                dto.setAprobada(aprobada);
                dto.setObservacion(aprobada ? "Aprobada" : "Reprobada");
            }

            resultado.add(dto);
        }

        return resultado;
    }
    /**
     * Determina si una materia del Excel debe considerarse aprobada.
     *
     * <p>El criterio de aprobación contempla dos casos:
     * <ul>
     *     <li>Una nota numérica definitiva mayor o igual a 3.0</li>
     *     <li>Aprobación mediante calificación por letra (según bandera del DTO)</li>
     * </ul>
     * Este mecanismo permite soportar planes antiguos donde algunas materias
     * se calificaban con letra en lugar de nota numérica.</p>
     *
     * @param m materia vista desde el Excel.
     * @return true si la materia está aprobada.
     */
    private boolean esAprobada(MateriaVistaExcel m) {
        return (m.getDefinitiva() != null && m.getDefinitiva() >= 3.0)
                || m.isAprobadaPorLetra(); // nuevo
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public DatosAcademicoResponse registrarDecisionFinal(Long idDatosAcademicos, boolean niveladoFinal) {
        // 1. Buscar datos académicos
        DatosAcademico datos = datosAcademicoRepository.findById(idDatosAcademicos)
                .orElseThrow(() -> new ResourceNotFoundException("No existe registro académico para el ID: " + idDatosAcademicos));

        // 2.Validar estado previo
        if (datos.getEstadoAptitud() != EstadoAptitud.POSIBLE_NIVELADO) {
            throw new InvalidStateException("Solo se puede registrar decisión final para estudiantes 'Posibles Nivelados'.");
        }

        // 3. Actualizar estado según la decisión
        if (niveladoFinal) {
            datos.setEsNivelado(true);
            datos.setEstadoAptitud(EstadoAptitud.NIVELADO_CONFIRMADO);
        } else {
            datos.setEsNivelado(false);
            datos.setEstadoAptitud(EstadoAptitud.NIVELADO_DESCARTADO);
        }

        // 4. Actualizar booleano de nivelado
        datos.setEsNivelado(niveladoFinal);

        datosAcademicoRepository.save(datos);

        // 5. Armar respuesta
        return datosAcademicoMapper.toResponse(datos);
    }

    /**
     * Normaliza un nombre de materia para facilitar su comparación.
     *
     * <p>El proceso incluye:
     * <ul>
     *     <li>Eliminar acentos (uso de Normalizer)</li>
     *     <li>Convertir a minúsculas</li>
     *     <li>Eliminar espacios redundantes</li>
     * </ul>
     * Esto permite comparar "Cálculo diferencial", "CALCULO DIFERENCIAL" o "calculo diferencial"
     * de forma consistente.</p>
     *
     * @param texto cadena original.
     * @return texto normalizado en minúsculas y sin tildes.
     */
    private String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // elimina tildes
                .toLowerCase()
                .trim();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<DatosAcademicoResponse> preseleccionarNivelados(Long idPeriodo) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado"));

        // Validar que el período esté exactamente en el estado PROCESO_CARGA_SIMCA
        if (periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_CARGA_SIMCA) {
            throw new InvalidStateException("Solo se pueden preseleccionar nivelados después de cargar los datos de SIMCA.");
        }

        // 1. Obtener todos los datos académicos cargados para este período
        List<DatosAcademico> todosLosDatos =
                datosAcademicoRepository.findByRespuesta_Periodo_Id(idPeriodo);

        List<DatosAcademico> posiblesNivelados = new ArrayList<>();

        for (DatosAcademico datos : todosLosDatos) {

            PlanEstudio plan = datos.getPlanEstudios();

            if (plan.getReglasNivelacion() == null || plan.getReglasNivelacion().isEmpty()) {
                continue; // Si el plan no tiene reglas, se omite
            }

            Map<String, ReglaNivelacion> reglas = objectMapper.convertValue(
                    plan.getReglasNivelacion(),
                    new TypeReference<Map<String, ReglaNivelacion>>() {}
            );

            // Recorremos las reglas tipadas
            boolean esCandidato = reglas.values().stream()
                    .anyMatch(regla ->
                            datos.getCreditosAprobados() >= regla.getMinCreditosAprobados() &&
                                    datos.getPeriodosMatriculados() <= regla.getMaxPeriodosMatriculados()
                    );

            if (esCandidato) {
                datos.setEstadoAptitud(EstadoAptitud.POSIBLE_NIVELADO);
                posiblesNivelados.add(datos);
            }
        }
        // Guardamos los cambios de estado (solo a los marcados)
        datosAcademicoRepository.saveAll(posiblesNivelados);
        // Avanzar el estado del período
        periodo.setEstado(EstadoPeriodoAcademico.PROCESO_REVISION_POTENCIALES_NIVELADOS);
        periodoRepository.save(periodo);
        // HU 2.2.1.2: "Generar un reporte/listado"
        return posiblesNivelados.stream()
                .map(datosAcademicoMapper::toResponse)
                .toList();
    }
}


