package com.unicauca.fiet.sistema_electivas.periodo_academico.service;

import com.unicauca.fiet.sistema_electivas.common.exception.BusinessException;
import com.unicauca.fiet.sistema_electivas.common.exception.InvalidStateException;
import com.unicauca.fiet.sistema_electivas.common.exception.ResourceNotFoundException;
import com.unicauca.fiet.sistema_electivas.electiva.enums.EstadoElectiva;
import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.electiva.model.ProgramaElectiva;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ElectivaRepository;
import com.unicauca.fiet.sistema_electivas.electiva.repository.ProgramaElectivaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.EditarCuposDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.OfertaRequestDTO;
import com.unicauca.fiet.sistema_electivas.periodo_academico.dto.OfertaResponse;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoOferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.enums.EstadoPeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.mapper.OfertaMapper;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.OfertaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.PeriodoAcademicoRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfertaAcademicaServiceImpl implements OfertaAcademicaService {
    @Autowired
    private ElectivaRepository electivaRepository;
    @Autowired
    private PeriodoAcademicoRepository periodoRepository;
    @Autowired
    private OfertaRepository ofertaRepository;
    @Autowired
    private ProgramaElectivaRepository programaElectivaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OfertaResponse agregarElectivaOfertada(Long periodoId, OfertaRequestDTO dto) {
        // 1. Validar período
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException("Período no encontrado"));
        if (periodo.getEstado() != EstadoPeriodoAcademico.CONFIGURACION) {
            throw new InvalidStateException("No se pueden agregar electivas en un período no configurable.");
        }

        // 2. Validar electiva
        Electiva electiva = electivaRepository.findById(dto.getElectivaId())
                .orElseThrow(() -> new ResourceNotFoundException("Electiva no encontrada"));
        if (electiva.getEstado() != EstadoElectiva.APROBADA) {
            throw new BusinessException("Solo se pueden ofertar electivas aprobadas.");
        }

        // 3. Evitar duplicados
        boolean yaExiste = ofertaRepository.existsByElectivaIdAndPeriodoId(electiva.getId(), periodo.getId());
        if (yaExiste) {
            throw new BusinessException("La electiva " + electiva.getCodigo() + " - " + electiva.getNombre() +
                    " ya se encuentra en la oferta de este período.");
        }

        // 4. Copiar configuración de programas
        Map<Long, Integer> cupos = validarYCopiarCupos(dto.getCuposPorPrograma(), electiva);

        // 5. Crear y guardar oferta
        Oferta ofertada = OfertaMapper.toEntity(dto, electiva, periodo);
        Oferta guardada = ofertaRepository.save(ofertada);

        // 6. Transformar a DTO de respuesta
        return OfertaMapper.toResponse(guardada);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OfertaResponse editarCupos(Long ofertadaId, EditarCuposDTO dto) {
        // 1️. Obtener la electiva ofertada
        Oferta ofertada = ofertaRepository.findById(ofertadaId)
                .orElseThrow(() -> new ResourceNotFoundException("Electiva ofertada no encontrada."));

        // 2️. Validar que el periodo esté en estado CONFIGURACION
        PeriodoAcademico periodo = ofertada.getPeriodo();
        if (periodo.getEstado() != EstadoPeriodoAcademico.CONFIGURACION) {
            throw new InvalidStateException("No se pueden editar cupos fuera del estado CONFIGURACION.");
        }

        // 3. Validar el estado de la Oferta
        if (ofertada.getEstado() != EstadoOferta.OFERTADA) {
            throw new InvalidStateException("No se puede editar los cupos de una electiva fuera del estado OFERTADA.");
        }

        // 3️. Validar y normalizar los cupos
        Map<Long, Integer> cuposValidados = validarYCopiarCupos(dto.getCuposPorPrograma(), ofertada.getElectiva());

        // 4️. Actualizar campos
        ofertada.setCuposPorPrograma(cuposValidados);
        ofertada.setFechaActualizacion(Instant.now());

        // 5️. Guardar cambios
        Oferta actualizada = ofertaRepository.save(ofertada);

        // 6️. Devolver DTO actualizado
        return OfertaMapper.toResponse(actualizada);
    }
    /**
     * Valida y prepara los cupos por programa para una electiva ofertada.
     *
     * <p>Pasos realizados:
     * <ul>
     *   <li>Obtiene los programas asociados a la electiva desde la tabla intermedia {@code programa_electiva}.</li>
     *   <li>Si no se proporcionan cupos en el DTO, reparte automáticamente 18 cupos de manera equitativa entre los programas asociados.</li>
     *   <li>Si se proporcionan cupos, valida que:
     *       <ul>
     *           <li>Ningún ID de programa ni cupo sea nulo.</li>
     *           <li>Todos los programas estén asociados a la electiva.</li>
     *           <li>Los cupos sean positivos.</li>
     *       </ul>
     *   </li>
     *   <li>Valida que la suma total de cupos sea exactamente 18.</li>
     * </ul>
     *
     * @param cuposDto Mapa de IDs de programas a número de cupos, proveniente del DTO.
     * @param electiva Entidad {@link Electiva} asociada a la oferta.
     * @return Mapa final de cupos por programa, listo para asignar a {@link Oferta}.
     * @throws BusinessException si hay problemas de asociación, nulos o cupos inválidos.
     * @throws IllegalArgumentException si la suma total de cupos no es 18.
     */
    private Map<Long, Integer> validarYCopiarCupos(Map<Long, Integer> cuposDto, Electiva electiva) {
        Map<Long, Integer> cuposFinales = new HashMap<>();

        //  1. Obtener los programas asociados a la electiva desde la tabla intermedia
        List<ProgramaElectiva> asociaciones = programaElectivaRepository.findByElectivaId(electiva.getId());

        if (asociaciones.isEmpty()) {
            throw new BusinessException(
                    "La electiva no tiene programas asociados en programa_electiva.");
        }

        Set<Long> programasAsociados = asociaciones.stream()
                .map(pe -> pe.getPrograma().getId())
                .collect(Collectors.toSet());

        // 2. Si no hay cupos definidos, repartir los 18 equitativamente
        if (cuposDto == null || cuposDto.isEmpty()) {
            int totalProgramas = programasAsociados.size();
            int cuposPorPrograma = 18 / totalProgramas;
            int residuo = 18 % totalProgramas;

            for (Long idPrograma : programasAsociados) {
                cuposFinales.put(idPrograma, cuposPorPrograma);
            }

            // Asignar el residuo (si lo hay) al primer programa
            if (residuo > 0) {
                Long primerPrograma = programasAsociados.iterator().next();
                cuposFinales.put(primerPrograma, cuposFinales.get(primerPrograma) + residuo);
            }

            return cuposFinales;
        }

        // 3. Validar los cupos enviados desde el DTO
        int sumaTotal = 0;
        for (Map.Entry<Long, Integer> entry : cuposDto.entrySet()) {
            Long programaId = entry.getKey();
            Integer cupos = entry.getValue();

            // Validar tipos y nulos
            if (programaId == null || cupos == null) {
                throw new BusinessException("Los IDs de programa y cupos no pueden ser nulos.");
            }

            // Validar asociación con la electiva
            if (!programasAsociados.contains(programaId)) {
                throw new BusinessException(
                        String.format("El programa con ID %d no está asociado a la electiva.", programaId)
                );
            }

            // Validar cupos positivos
            if (cupos <= 0) {
                throw new BusinessException(
                        String.format("El número de cupos para el programa %d debe ser mayor que 0.", programaId)
                );
            }

            cuposFinales.put(programaId, cupos);
            sumaTotal += cupos;
        }

        // 4. Validar suma total de 18 cupos exactos
        if (sumaTotal != 18) {
            throw new BusinessException(
                    String.format("La suma total de cupos (%d) debe ser exactamente 18.", sumaTotal)
            );
        }

        return cuposFinales;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminarElectivaOfertada(Long ofertadaId) {
        // 1️. Buscar la electiva ofertada
        Oferta ofertada = ofertaRepository.findById(ofertadaId)
                .orElseThrow(() -> new ResourceNotFoundException("Electiva ofertada no encontrada."));

        // 2️. Validar estado del periodo
        if (ofertada.getPeriodo().getEstado() != EstadoPeriodoAcademico.CONFIGURACION) {
            throw new InvalidStateException("No se puede eliminar una electiva en un periodo academico fuera del estado CONFIGURACION.");
        }

        // 3. Validar el estado de la Oferta
        if (ofertada.getEstado() != EstadoOferta.OFERTADA) {
            throw new InvalidStateException("No se puede eliminar una electiva fuera del estado OFERTADA.");
        }

        // 3️. Eliminar registro
        ofertaRepository.delete(ofertada);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<OfertaResponse> listarElectivasPorPeriodo(Long periodoId) {

        // Validar existencia del período
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el período académico con ID " + periodoId
                ));

        List<Oferta> electivas = ofertaRepository.findByPeriodoId(periodoId);

        return electivas.stream()
                .map(OfertaMapper::toResponse)
                .collect(Collectors.toList());
    }

}
