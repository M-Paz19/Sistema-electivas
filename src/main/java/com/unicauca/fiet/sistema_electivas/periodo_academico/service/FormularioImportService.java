package com.unicauca.fiet.sistema_electivas.periodo_academico.service;

import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.Oferta;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestaOpcion;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.OfertaRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestaOpcionRepository;
import com.unicauca.fiet.sistema_electivas.periodo_academico.repository.RespuestasFormularioRepository;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import com.unicauca.fiet.sistema_electivas.programa.repository.ProgramaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Servicio encargado de procesar e importar las respuestas de los formularios
 * cargados por los estudiantes en un período académico.
 *
 * <p>Este servicio toma los datos crudos leídos (por ejemplo, desde un archivo CSV o Excel),
 * los convierte en entidades del dominio {@link RespuestasFormulario} y sus
 * opciones asociadas ({@link RespuestaOpcion}), y los persiste en la base de datos.</p>
 *
 * <p>Durante el proceso:
 * <ul>
 *   <li>Asocia cada respuesta al {@link PeriodoAcademico} correspondiente.</li>
 *   <li>Identifica el {@link Programa} académico del estudiante.</li>
 *   <li>Relaciona cada opción elegida con su {@link Oferta} correspondiente.</li>
 * </ul>
 * </p>
 *
 * <p>Este servicio no realiza validaciones académicas ni reglas de negocio complejas;
 * su función principal es registrar correctamente la información recibida.
 * La validación posterior de las respuestas se realiza en el dominio
 * {@code procesamientovalidacion}.</p>
 */

@Service
@RequiredArgsConstructor
public class FormularioImportService {

    private final ProgramaRepository programaRepository;
    private final RespuestasFormularioRepository respuestaRepository;
    private final RespuestaOpcionRepository opcionRepository;
    private final OfertaRepository ofertaRepository;
    /**
     * Procesa e inserta en la base de datos las respuestas de un formulario cargado.
     *
     * @param datosCrudos lista de mapas con los datos extraídos del archivo (una fila por estudiante)
     * @param periodo período académico al cual pertenecen las respuestas
     * @param archivo entidad que representa el archivo cargado originalmente
     * @return lista de entidades {@link RespuestasFormulario} creadas y persistidas
     */
    public List<RespuestasFormulario> procesarRespuestas(
            List<Map<String, String>> datosCrudos,
            PeriodoAcademico periodo,
            CargaArchivo archivo) {

        List<RespuestasFormulario> entidades = new ArrayList<>();

        for (Map<String, String> datos : datosCrudos) {
            RespuestasFormulario r = new RespuestasFormulario();
            r.setPeriodo(periodo);
            r.setArchivoCargado(archivo);
            r.setCodigoEstudiante(datos.get("Código del estudiante"));
            r.setCorreoEstudiante(datos.get("Correo institucional"));
            r.setNombreEstudiante(datos.get("Nombre"));
            r.setApellidosEstudiante(datos.get("Apellidos"));
            String ts = datos.get("timestampRespuesta");
            if (ts != null) {
                r.setTimestampRespuesta(Instant.parse(ts)); // Usa la hora real del envío
            } else {
                r.setTimestampRespuesta(Instant.now()); // fallback
            }


            // Buscar programa
            String progTexto = datos.get("Programa académico");
            programaRepository.findByNombreIgnoreCase(progTexto).ifPresent(r::setPrograma);

            // Guardar cabecera
            respuestaRepository.save(r);

            // Crear opciones dinámicas
            short num = 1;
            for (String key : datos.keySet()) {
                if (key.startsWith("Electiva opción")) {
                    String electivaTexto = datos.get(key);

                    RespuestaOpcion op = new RespuestaOpcion();
                    op.setRespuesta(r);
                    op.setOpcionNum(num++);

                    // Buscar oferta por nombre de electiva y periodo
                    ofertaRepository.findByElectivaNombreIgnoreCaseAndPeriodo(electivaTexto, periodo)
                            .ifPresent(op::setOferta);

                    opcionRepository.save(op);
                }
            }

            entidades.add(r);
        }

        return entidades;
    }
}
