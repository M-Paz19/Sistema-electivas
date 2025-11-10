package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.plan_estudio.enums.TipoMateria;
import com.unicauca.fiet.sistema_electivas.plan_estudio.model.PlanMateria;
import com.unicauca.fiet.sistema_electivas.plan_estudio.repository.PlanMateriaRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.MateriaVistaExcel;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.MateriaComparadaDTO;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.ValidacionNiveladoResponseDTO;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.VerificacionNiveladoDTO;
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

@Service
@RequiredArgsConstructor
public class ValidacionNiveladosServiceImpl implements ValidacionNiveladosService {
    @Autowired
    private DatosAcademicoRepository datosAcademicoRepository;
    @Autowired
    private PlanMateriaRepository planMateriaRepository;
    @Autowired
    private ExcelHistorialAcademicoService  excelHistorialAcademicoService;
    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public VerificacionNiveladoDTO generarReporteNivelado(MultipartFile archivoExcel, Long idDatosAcademicos) {
        // 1. Buscar datos académicos
        DatosAcademico datos = datosAcademicoRepository.findById(idDatosAcademicos)
                .orElseThrow(() -> new ResourceNotFoundException("No existe registro académico para el ID: " + idDatosAcademicos));

        // 2. Validar estado actual
        /**
        if (!"POSIBLE_NIVELADO".equalsIgnoreCase(datos.getEstadoAptitud())) {
            throw new InvalidStateException("El estudiante no está en estado 'POSIBLE_NIVELADO'.");
        }*/

        // 3. Obtener número de semestres cursados
        int semestres = datos.getPeriodosMatriculados();

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
        // 8. Determinar si está nivelado todo aprobado

        // 9. Construir DTO de salida
        VerificacionNiveladoDTO resultado = new VerificacionNiveladoDTO();
        resultado.setCodigoEstudiante(datos.getCodigoEstudiante());
        resultado.setNombre(datos+" "+datos.getApellidos() + " (" + datos.getUsuario() + ")");//cambiar en futuro proximo
        resultado.setPrograma(datos.getPrograma());//cambiar en futuro proximo
        resultado.setSemestresMatriculados(semestres);
        resultado.setPorcentajeAvance(porcentaje);
        resultado.setNivelado(todasAprobadas);
        resultado.setComparacionMaterias(comparacion);
        // Obtener materias no aprobadas
        List<String> noAprobadas = comparacion.stream()
                .filter(m -> !m.isAprobada())
                .map(MateriaComparadaDTO::getNombre) // o el atributo que corresponda
                .toList();

        // Armar mensaje
        String mensaje;
        if (noAprobadas.isEmpty()) {
            mensaje = "Todas las materias del plan hasta el semestre " + semestres + " están aprobadas.";
        } else {
            mensaje = "Materias pendientes por aprobar: " + String.join(", ", noAprobadas);
        }
        // Asignar mensaje al DTO
        resultado.setMensajeResumen(mensaje);
        return resultado;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public ValidacionNiveladoResponseDTO registrarDecisionFinal(Long idDatosAcademicos, boolean niveladoFinal) {
        // 1. Buscar datos académicos
        DatosAcademico datos = datosAcademicoRepository.findById(idDatosAcademicos)
                .orElseThrow(() -> new ResourceNotFoundException("No existe registro académico para el ID: " + idDatosAcademicos));

        // 2. (Opcional) Validar estado previo
        /*
        if (!"POSIBLE_NIVELADO".equalsIgnoreCase(datos.getEstadoAptitud())) {
            throw new InvalidStateException("El estudiante no está en estado 'POSIBLE_NIVELADO'.");
        }
        *///futuro proximo

        // 3. Actualizar estado según la decisión
        //datos.setEstadoAptitud(niveladoFinal ? "NIVELADO" : "NO_NIVELADO");Futuro proximo

        // 4. Actualizar booleano de nivelado
        datos.setEsNivelado(niveladoFinal);


        datosAcademicoRepository.save(datos);

        // 5. Armar respuesta
        ValidacionNiveladoResponseDTO response = new ValidacionNiveladoResponseDTO();
        response.setIdDatosAcademicos(datos.getId());
        response.setCodigoEstudiante(datos.getCodigoEstudiante());
        response.setNombreCompleto(datos.getApellidos() + " " + datos.getUsuario());
        response.setPrograma(datos.getPrograma());
        response.setEstado(datos.getEstadoAptitud());


        return response;
    }
    /**
     * Compara las materias del plan de estudios con las materias aprobadas
     * registradas en el historial académico (archivo Excel).
     *
     * <p>El proceso identifica si cada materia del plan fue aprobada, está pendiente,
     * o fue reprobada. Adicionalmente maneja casos especiales como materias tipo
     * <b>FISH</b> y <b>Electivas</b>, las cuales se validan por cantidad aprobada
     * y no por nombre exacto.</p>
     *
     * @param plan lista de materias correspondientes al plan de estudios del estudiante
     * @param materiasVistas lista de materias cursadas y aprobadas extraídas desde el Excel
     * @return lista de objetos {@link MateriaComparadaDTO} que detallan la comparación materia a materia
     */
    private List<MateriaComparadaDTO> compararMaterias(List<PlanMateria> plan, List<MateriaVistaExcel> materiasVistas) {
        List<MateriaComparadaDTO> resultado = new ArrayList<>();
       // --- Agrupar materias vistas ---
        List<MateriaVistaExcel> fishAprobadas = materiasVistas.stream()
                .filter(m -> normalizar(m.getNombre()).contains("fish"))
                .filter(m -> m.getDefinitiva() != null && m.getDefinitiva() >= 3.0)
                .toList();

        List<MateriaVistaExcel> electivasAprobadas = materiasVistas.stream()
                .filter(m -> normalizar(m.getNombre()).contains("electiva")
                        && !normalizar(m.getNombre()).contains("fish"))
                .filter(m -> m.getDefinitiva() != null && m.getDefinitiva() >= 3.0)
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
                boolean aprobada = coincidencias.stream()
                        .anyMatch(m -> m.getDefinitiva() != null && m.getDefinitiva() >= 3.0);
                dto.setAprobada(aprobada);
                dto.setObservacion(aprobada ? "Aprobada" : "Reprobada");
            }

            resultado.add(dto);
        }

        return resultado;
    }
    /**
     * Normaliza el texto de los nombres de materias para facilitar su comparación.
     *
     * <p>El método elimina tildes, convierte a minúsculas y recorta espacios
     * innecesarios. Esto permite comparar nombres de materias que puedan
     * tener diferencias menores en acentuación o formato.</p>
     *
     * @param texto cadena de texto a normalizar
     * @return versión normalizada del texto, sin tildes y en minúsculas
     */
    private String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // elimina tildes
                .toLowerCase()
                .trim();
    }

}


