package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.archivo.service.ArchivoService;
import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoRespuestaFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.CambioEstadoValidacionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioDesicionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.RespuestaFormularioMapper;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.mapper.ValidacionProcesamientoMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValidacionRespuestasFormsServiceImpl implements ValidacionRespuestasFormsService {
    @Autowired
    private RespuestasFormularioRepository respuestasRepository;
    @Autowired
    private PeriodoAcademicoRepository periodoRepository;
    @Autowired
    private ArchivoService archivoService;

    @Transactional
    @Override
    public List<RespuestaFormularioResponse> obtenerRespuestasPorPeriodo(Long periodoId) {
        List<RespuestasFormulario> entidades = respuestasRepository.findByPeriodoId(periodoId);
        return RespuestaFormularioMapper.toResponseList(entidades);
    }

    @Transactional
    @Override
    public CambioEstadoValidacionResponse aplicarFiltroDuplicados(Long idPeriodo) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado"));

        if (periodo.getEstado() != EstadoPeriodoAcademico.CERRADO_FORMULARIO &&
                periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_FILTRADO_DUPLICADOS) {
            throw new InvalidStateException("El filtro de duplicados solo puede aplicarse cuando el formulario está cerrado.");
        }

        List<RespuestasFormulario> respuestas = respuestasRepository.findByPeriodoId(idPeriodo).stream()
                .filter(r -> r.getEstado() == EstadoRespuestaFormulario.SIN_PROCESAR ||
                        r.getEstado() == EstadoRespuestaFormulario.UNICO ||
                        r.getEstado() == EstadoRespuestaFormulario.DUPLICADO)
                .collect(Collectors.toList());

        if (respuestas.isEmpty()) {
            // Si está vacío, no es error, simplemente no hay nada que procesar
        }

        Map<String, List<RespuestasFormulario>> agrupadas = respuestas.stream()
                .collect(Collectors.groupingBy(RespuestasFormulario::getCodigoEstudiante));

        int duplicadosEliminados = 0;
        int conservadas = 0;

        for (var entry : agrupadas.entrySet()) {
            List<RespuestasFormulario> grupo = entry.getValue();
            grupo.sort(Comparator.comparing(RespuestasFormulario::getTimestampRespuesta));

            RespuestasFormulario primera = grupo.get(0);
            primera.setEstado(EstadoRespuestaFormulario.UNICO);
            conservadas++;

            for (int i = 1; i < grupo.size(); i++) {
                RespuestasFormulario duplicada = grupo.get(i);
                duplicada.setEstado(EstadoRespuestaFormulario.DUPLICADO);
                duplicadosEliminados++;
            }
        }

        respuestasRepository.saveAll(respuestas);

        if (periodo.getEstado() == EstadoPeriodoAcademico.CERRADO_FORMULARIO) {
            periodo.setEstado(EstadoPeriodoAcademico.PROCESO_FILTRADO_DUPLICADOS);
            periodoRepository.save(periodo);
        }

        return ValidacionProcesamientoMapper.toCambioEstadoResponse(
                periodo,
                String.format("Filtrado completado: %d duplicados eliminados, %d registros únicos conservados.",
                        duplicadosEliminados, conservadas)
        );
    }

    @Transactional
    @Override
    public CambioEstadoValidacionResponse aplicarFiltroCodigosPorAntiguedad(Long idPeriodo) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado"));

        if (periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_FILTRADO_DUPLICADOS &&
                periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_CLASIFICACION_ANTIGUEDAD) {
            throw new InvalidStateException(
                    "El filtro de código estudiantil solo puede aplicarse cuando ya se aplicó el filtro de duplicados."
            );
        }

        List<RespuestasFormulario> respuestas = respuestasRepository.findByPeriodoId(idPeriodo).stream()
                .filter(r -> r.getEstado() == EstadoRespuestaFormulario.UNICO ||
                        r.getEstado() == EstadoRespuestaFormulario.CUMPLE ||
                        r.getEstado() == EstadoRespuestaFormulario.NO_CUMPLE ||
                        r.getEstado() == EstadoRespuestaFormulario.FORMATO_INVALIDO)
                .collect(Collectors.toList());

        Pattern patron = Pattern.compile("^\\d{4}(\\d{2})(0[1-2])\\d{4}$");
        String[] partes = periodo.getSemestre().split("-");
        int anioActual = Integer.parseInt(partes[0]);
        int periodoActual = Integer.parseInt(partes[1]);

        respuestas.forEach(r -> {
            // Si ya fue incluido manualmente, no lo tocamos
            if (r.getEstado() == EstadoRespuestaFormulario.INCLUIDO) return;

            String codigo = r.getCodigoEstudiante();
            Matcher matcher = patron.matcher(codigo);

            if (!matcher.matches()) {
                r.setEstado(EstadoRespuestaFormulario.FORMATO_INVALIDO);
            } else {
                int anioIngreso = 2000 + Integer.parseInt(matcher.group(1));
                int periodoIngreso = Integer.parseInt(matcher.group(2));
                int semestres = ((anioActual - anioIngreso) * 2) + (periodoActual - periodoIngreso);
                r.setEstado(semestres >= 6 ? EstadoRespuestaFormulario.CUMPLE : EstadoRespuestaFormulario.NO_CUMPLE);
            }
        });

        respuestasRepository.saveAll(respuestas);

        if (periodo.getEstado() == EstadoPeriodoAcademico.PROCESO_FILTRADO_DUPLICADOS) {
            periodo.setEstado(EstadoPeriodoAcademico.PROCESO_CLASIFICACION_ANTIGUEDAD);
            periodoRepository.save(periodo);
        }

        return ValidacionProcesamientoMapper.toCambioEstadoResponse(
                periodo,
                "Filtro de antigüedad aplicado con éxito"
        );
    }

    @Transactional
    @Override
    public RespuestaFormularioDesicionResponse revisarManualFormatoInvalido(Long respuestaId, boolean incluir, @Nullable String nuevoCodigo) {
        RespuestasFormulario respuesta = respuestasRepository.findById(respuestaId)
                .orElseThrow(() -> new ResourceNotFoundException("Respuesta no encontrada"));

        // Validación de estado relajada para permitir correcciones
        if (respuesta.getEstado() != EstadoRespuestaFormulario.FORMATO_INVALIDO &&
                respuesta.getEstado() != EstadoRespuestaFormulario.INCLUIDO &&
                respuesta.getEstado() != EstadoRespuestaFormulario.SIN_PROCESAR) {
            // Permitimos editar si está en uno de estos estados
        }

        if (incluir) {
            if (nuevoCodigo == null || nuevoCodigo.trim().isEmpty()) {
                throw new BusinessException("Para incluir al estudiante, debe proporcionar un código válido.");
            }

            String codigoLimpio = nuevoCodigo.trim();

            // CORRECCIÓN: Verificar duplicados EXCLUYENDO la respuesta actual
            // Usamos stream filter para mayor seguridad si el repo no tiene el método custom
            boolean existe = respuestasRepository.findByPeriodoId(respuesta.getPeriodo().getId()).stream()
                    .anyMatch(r -> r.getCodigoEstudiante().equals(codigoLimpio)
                            && !r.getId().equals(respuestaId) // Importante: excluir la actual
                            && (r.getEstado() == EstadoRespuestaFormulario.UNICO ||
                            r.getEstado() == EstadoRespuestaFormulario.CUMPLE ||
                            r.getEstado() == EstadoRespuestaFormulario.INCLUIDO ||
                            r.getEstado() == EstadoRespuestaFormulario.DATOS_CARGADOS));

            if (existe) {
                throw new BusinessException("El código " + codigoLimpio + " ya existe y está activo en este periodo.");
            }

            respuesta.setCodigoEstudiante(codigoLimpio);
            respuesta.setEstado(EstadoRespuestaFormulario.INCLUIDO);
        } else {
            respuesta.setEstado(EstadoRespuestaFormulario.DESCARTADO);
        }

        // Guardar cambios inmediatamente
        RespuestasFormulario guardada = respuestasRepository.saveAndFlush(respuesta);

        return ValidacionProcesamientoMapper.toDecisionResponse(guardada,
                incluir ? "Estudiante incluido." : "Estudiante descartado.");
    }

    @Transactional
    @Override
    public CambioEstadoValidacionResponse confirmarListaParaSimca(Long idPeriodo) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo académico no encontrado."));

        // Validar estado
        if (periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_CLASIFICACION_ANTIGUEDAD &&
                periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_CONFIRMACION_SIMCA) {
            throw new InvalidStateException("El período no está listo para la confirmación final.");
        }

        long pendientes = respuestasRepository.findByPeriodoId(idPeriodo).stream()
                .filter(r -> r.getEstado() == EstadoRespuestaFormulario.FORMATO_INVALIDO)
                .count();

        if (pendientes > 0) {
            throw new InvalidStateException(
                    "Faltan " + pendientes + " respuestas por revisar manualmente antes de confirmar la lista."
            );
        }

        // Obtener códigos válidos: CUMPLE o INCLUIDO
        List<String> codigosValidos = respuestasRepository.findByPeriodoId(idPeriodo).stream()
                .filter(r -> r.getEstado() == EstadoRespuestaFormulario.CUMPLE ||
                        r.getEstado() == EstadoRespuestaFormulario.INCLUIDO)
                .map(RespuestasFormulario::getCodigoEstudiante)
                .distinct()
                .collect(Collectors.toList());

        long descartados = respuestasRepository.countByPeriodoIdAndEstadoIn(
                idPeriodo,
                List.of(EstadoRespuestaFormulario.DESCARTADO, EstadoRespuestaFormulario.NO_CUMPLE, EstadoRespuestaFormulario.DUPLICADO)
        );

        if (codigosValidos.isEmpty()) {
            throw new InvalidStateException("No hay códigos válidos para generar lotes SIMCA.");
        }

        List<List<String>> lotes = dividirEnLotes(codigosValidos, 50);
        archivoService.generarArchivosLotesSimca(lotes, periodo);

        periodo.setEstado(EstadoPeriodoAcademico.PROCESO_CONFIRMACION_SIMCA);
        periodoRepository.save(periodo);

        String mensajeFinal = String.format(
                "Confirmación final realizada. Se generaron %d lotes con %d códigos válidos. Se descartaron %d respuestas.",
                lotes.size(), codigosValidos.size(), descartados
        );
        return ValidacionProcesamientoMapper.toCambioEstadoResponse(periodo, mensajeFinal);
    }

    private List<List<String>> dividirEnLotes(List<String> lista, int tamLote) {
        List<List<String>> lotes = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += tamLote) {
            lotes.add(lista.subList(i, Math.min(i + tamLote, lista.size())));
        }
        return lotes;
    }
}