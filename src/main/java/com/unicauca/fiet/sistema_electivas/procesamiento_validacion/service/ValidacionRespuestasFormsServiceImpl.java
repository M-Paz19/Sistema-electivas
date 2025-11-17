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

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<RespuestaFormularioResponse> obtenerRespuestasPorPeriodo(Long periodoId) {
        List<RespuestasFormulario> entidades = respuestasRepository.findByPeriodoId(periodoId);
        return RespuestaFormularioMapper.toResponseList(entidades);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public CambioEstadoValidacionResponse aplicarFiltroDuplicados(Long idPeriodo) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado"));

        if (periodo.getEstado() != EstadoPeriodoAcademico.CERRADO_FORMULARIO) {
            throw new InvalidStateException("El filtro de duplicados solo puede aplicarse cuando el formulario está cerrado.");
        }

        List<RespuestasFormulario> respuestas = respuestasRepository
                .findByPeriodoIdAndEstado(idPeriodo, EstadoRespuestaFormulario.SIN_PROCESAR);

        if (respuestas.isEmpty()) {
            throw new BusinessException("No hay respuestas sin procesar para este período.");
        }
        Map<String, List<RespuestasFormulario>> agrupadas = respuestas.stream()
                .collect(Collectors.groupingBy(RespuestasFormulario::getCodigoEstudiante));

        int duplicadosEliminados = 0;
        int conservadas = 0;

        for (var entry : agrupadas.entrySet()) {
            List<RespuestasFormulario> grupo = entry.getValue();
            grupo.sort(Comparator.comparing(RespuestasFormulario::getTimestampRespuesta));
            RespuestasFormulario primera = grupo.get(0);
            conservadas++;

            for (int i = 1; i < grupo.size(); i++) {
                RespuestasFormulario duplicada = grupo.get(i);
                duplicada.setEstado(EstadoRespuestaFormulario.DUPLICADO);
                duplicadosEliminados++;
            }

            primera.setEstado(EstadoRespuestaFormulario.UNICO);
        }

        respuestasRepository.saveAll(respuestas);

        System.out.printf("X=%d registros duplicados eliminados. Y=%d registros únicos conservados.%n",
                duplicadosEliminados, conservadas);

        periodo.setEstado(EstadoPeriodoAcademico.PROCESO_FILTRADO_DUPLICADOS);
        periodoRepository.save(periodo);
        return ValidacionProcesamientoMapper.toCambioEstadoResponse(
                periodo,
                String.format("Filtrado completado: %d duplicados eliminados, %d registros únicos conservados.",
                        duplicadosEliminados, conservadas)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public CambioEstadoValidacionResponse aplicarFiltroCodigosPorAntiguedad(Long idPeriodo) {
        // Buscar el período
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado"));

        // Validar estado del período
        if (periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_FILTRADO_DUPLICADOS) {
            throw new InvalidStateException(
                    "El filtro de código estudiantil solo puede aplicarse cuando ya se aplicó el filtro de duplicados."
            );
        }

        // Obtener respuestas únicas del período
        List<RespuestasFormulario> respuestas = respuestasRepository
                .findByPeriodoIdAndEstado(idPeriodo, EstadoRespuestaFormulario.UNICO);

        if (respuestas.isEmpty()) {
            throw new BusinessException("No hay respuestas únicas válidas para este período.");
        }

        // Patrón de código estudiantil: 104621011351 → año ingreso = 21, periodo = 01
        Pattern patron = Pattern.compile("^\\d{4}(\\d{2})(0[1-2])\\d{4}$");

        // Extraer año y periodo actual del formato "2027-1"
        String[] partes = periodo.getSemestre().split("-");
        int anioActual = Integer.parseInt(partes[0]);
        int periodoActual = Integer.parseInt(partes[1]);

        // Clasificación por antigüedad
        respuestas.forEach(r -> {
            String codigo = r.getCodigoEstudiante();
            Matcher matcher = patron.matcher(codigo);

            if (!matcher.matches()) {
                r.setEstado(EstadoRespuestaFormulario.FORMATO_INVALIDO);
                return;
            }

            int anioIngreso = 2000 + Integer.parseInt(matcher.group(1));  // 21 → 2021
            int periodoIngreso = Integer.parseInt(matcher.group(2));      // 01 → 1, 02 → 2

            int semestres = ((anioActual - anioIngreso) * 2) + (periodoActual - periodoIngreso);
            r.setEstado(semestres >= 6
                    ? EstadoRespuestaFormulario.CUMPLE
                    : EstadoRespuestaFormulario.NO_CUMPLE);
        });

        // Persistir resultados y actualizar el estado del período
        respuestasRepository.saveAll(respuestas);
        periodo.setEstado(EstadoPeriodoAcademico.PROCESO_CLASIFICACION_ANTIGUEDAD);
        periodoRepository.save(periodo);

        return ValidacionProcesamientoMapper.toCambioEstadoResponse(
                periodo,
                "Filtro de antigüedad aplicado con éxito"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public RespuestaFormularioDesicionResponse revisarManualFormatoInvalido(Long respuestaId, boolean incluir, @Nullable String nuevoCodigo) {
        // Buscar respuesta
        RespuestasFormulario respuesta = respuestasRepository.findById(respuestaId)
                .orElseThrow(() -> new ResourceNotFoundException("Respuesta no encontrada."));

        // Validar que esté en el estado correcto
        if (respuesta.getEstado() != EstadoRespuestaFormulario.FORMATO_INVALIDO) {
            throw new InvalidStateException("Solo se pueden revisar respuestas con formato desconocido.");
        }

        // Aplicar decisión
        if (incluir) {
            respuesta.setEstado(EstadoRespuestaFormulario.INCLUIDO);
        } else {
            respuesta.setEstado(EstadoRespuestaFormulario.DESCARTADO);
        }

        respuestasRepository.save(respuesta);
        return RespuestaFormularioMapper.toRespuestaFormularioResponse(respuesta);
    }


    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public CambioEstadoValidacionResponse confirmarListaParaSimca(Long idPeriodo) {
        PeriodoAcademico periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new ResourceNotFoundException("Periodo académico no encontrado."));

        // 1. Validar estado del periodo
        if (periodo.getEstado() != EstadoPeriodoAcademico.PROCESO_CLASIFICACION_ANTIGUEDAD) {
            throw new InvalidStateException("El período no está listo para la confirmación final. Debe haberse completado la clasificación de antigüedad.");
        }

        // 2. Verificar respuestas pendientes
        List<RespuestasFormulario> pendientes = respuestasRepository
                .findByPeriodoIdAndEstado(idPeriodo, EstadoRespuestaFormulario.FORMATO_INVALIDO);

        if (!pendientes.isEmpty()) {
            throw new InvalidStateException(
                    "Faltan " + pendientes.size() + " respuestas por revisar manualmente antes de confirmar la lista para SIMCA."
            );
        }

        // 3. Obtener todos los códigos válidos (CUMPLE o INCLUIDO_MANUAL)
        List<String> codigosValidos = respuestasRepository.findCodigosByPeriodoAndEstados(
                idPeriodo,
                List.of(
                        EstadoRespuestaFormulario.CUMPLE,
                        EstadoRespuestaFormulario.INCLUIDO
                )
        );

        // 4. Obtener cantidad de descartados (para el mensaje)
        long descartados = respuestasRepository.countByPeriodoIdAndEstadoIn(
                idPeriodo,
                List.of(
                        EstadoRespuestaFormulario.DESCARTADO,
                        EstadoRespuestaFormulario.NO_CUMPLE,
                        EstadoRespuestaFormulario.DUPLICADO
                )
        );
        if (codigosValidos.isEmpty()) {
            throw new InvalidStateException("No hay códigos válidos para generar lotes SIMCA.");
        }

        // 5. Dividir en lotes de 50
        List<List<String>> lotes = dividirEnLotes(codigosValidos, 50);

        // 6. Generar archivos de lotes con ArchivoService
        archivoService.generarArchivosLotesSimca(lotes, periodo);

        // 7. Cambiar estado del periodo
        periodo.setEstado(EstadoPeriodoAcademico.PROCESO_CONFIRMACION_SIMCA);
        periodoRepository.save(periodo);
        // 8. Construir mensaje informativo
        String mensajeFinal = String.format(
                "Confirmación final realizada. Se generaron %d lotes con %d códigos válidos para SIMCA. "
                        + "Se descartaron %d respuestas por no cumplir los criterios.",
                lotes.size(),
                codigosValidos.size(),
                descartados
        );
        return ValidacionProcesamientoMapper.toCambioEstadoResponse(periodo,mensajeFinal);
    }

    /**
     * Divide una lista en sublistas de tamaño máximo definido.
     */
    private List<List<String>> dividirEnLotes(List<String> lista, int tamLote) {
        List<List<String>> lotes = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += tamLote) {
            lotes.add(lista.subList(i, Math.min(i + tamLote, lista.size())));
        }
        return lotes;
    }


}
