package com.unicauca.fiet.sistema_electivas.procesamiento_validacion.service;

import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.DatosAcademicoResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioDesicionResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.RespuestaFormularioResponse;
import com.unicauca.fiet.sistema_electivas.procesamiento_validacion.dto.SimcaCargaResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ValidacionAcademicaService {


    SimcaCargaResponse cargarYValidarDatosSimca(Long idPeriodo, List<MultipartFile> archivos);
    List<RespuestaFormularioResponse> obtenerInconsistencias(Long idPeriodo);

    /**
     * HU 2.1.2.1: Corrige el código de estudiante de una respuesta inconsistente...
     */
    RespuestaFormularioDesicionResponse corregirCodigoEstudiante(Long respuestaId, String nuevoCodigo);

    /**
     * HU 2.1.2.2: Toma la decisión de incluir (FORZAR_INCLUSION) o descartar...
     */
    RespuestaFormularioDesicionResponse tomarDecisionInconsistencia(Long respuestaId, boolean incluir);

    String regenerarLoteCorregidos(Long idPeriodo);

    List<DatosAcademicoResponse> preseleccionarNivelados(Long idPeriodo);
}