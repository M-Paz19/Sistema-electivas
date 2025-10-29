package com.unicauca.fiet.sistema_electivas.integracion.google;


import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.model.*;

import com.unicauca.fiet.sistema_electivas.common.exception.GoogleFormsException;
import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
/**
 * Cliente para la integración con la API de Google Forms.
 *
 * <p>Permite crear, consultar y cerrar formularios de Google Forms desde el sistema académico.
 * Su función principal es abstraer la comunicación con la API de Google, proporcionando
 * operaciones de alto nivel reutilizables por los servicios del dominio {@code periodoacademico}.</p>
 *
 * <p>Acciones soportadas:
 * <ul>
 *   <li>Creación dinámica de formularios de preinscripción de electivas.</li>
 *   <li>Consulta y conversión de respuestas recibidas en estructuras manipulables.</li>
 *   <li>Cierre de formularios una vez finalizado el período de inscripción.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleFormsClient {
    /** Cliente oficial de la API de Google Forms, inyectado mediante configuración de seguridad OAuth. */
    private final Forms formsService;

    // --------------------------------------------------------
    // MÉTODOS PRINCIPALES DE INTEGRACIÓN
    // --------------------------------------------------------

    /**
     * Obtiene todas las respuestas enviadas a un formulario de Google Forms y las devuelve como respuesta.
     *
     * <p>Acciones realizadas:
     * <ul>
     *   <li>Consulta la API de Google Forms para obtener las respuestas.</li>
     *   <li>Mapea cada respuesta a una entidad {@link RespuestasFormulario}.</li>
     *   <li>Devuelve todas las respuestas en la base de datos asociadas al período correspondiente.</li>
     * </ul>
     *
     * @param formId  ID del formulario de Google Forms.
     * @throws RuntimeException Si ocurre un error al obtener o procesar las respuestas.
     */
    public List<Map<String, String>> obtenerRespuestas(String formId) {
        try {
            // 🔹 Obtener estructura del formulario
            Form form = formsService.forms().get(formId).execute();

            // 🔹 Construir mapa de questionId → título de la pregunta
            Map<String, String> mapaPreguntas = new HashMap<>();
            for (Item item : form.getItems()) {
                if (item.getQuestionItem() != null && item.getQuestionItem().getQuestion() != null) {
                    mapaPreguntas.put(item.getQuestionItem().getQuestion().getQuestionId(), item.getTitle());
                }
            }

            // 🔹 Obtener respuestas
            ListFormResponsesResponse response = formsService.forms().responses().list(formId).execute();
            if (response.getResponses() == null) return Collections.emptyList();

            List<Map<String, String>> respuestas = new ArrayList<>();

            for (FormResponse fr : response.getResponses()) {
                respuestas.add(convertirAmapa(fr, mapaPreguntas));
            }

            return respuestas;

        } catch (IOException e) {
            throw new GoogleFormsException("Error al obtener respuestas del formulario", e);
        }
    }


    /**
     * Convierte una respuesta completa de la API de Google Forms a un mapa llave-valor.
     *
     * <p>El mapa resultante contiene como clave el identificador de la pregunta y como valor
     * el texto respondido por el estudiante.</p>
     *
     * @param fr objeto {@link FormResponse} obtenido de la API de Google Forms.
     * @return mapa con las preguntas y respuestas textuales.
     */
    private Map<String, String> convertirAmapa(FormResponse fr, Map<String, String> mapaPreguntas) {
        Map<String, String> mapa = new HashMap<>();

        if (fr.getAnswers() != null) {
            fr.getAnswers().forEach((id, ans) -> {
                String pregunta = mapaPreguntas.get(id);
                String respuesta = ans.getTextAnswers().getAnswers().get(0).getValue();
                mapa.put(pregunta, respuesta);
            });
        }

        // 👇 Agregamos la fecha real del envío del formulario
        mapa.put("timestampRespuesta", fr.getLastSubmittedTime());
        return mapa;
    }

    /**
     * Extrae el valor de texto asociado a una pregunta específica dentro de las respuestas del formulario.
     *
     * @param answers Mapa de respuestas del formulario (clave-pregunta, valor-respuesta).
     * @param key     Texto de la pregunta que se desea buscar.
     * @return Valor de texto de la respuesta, o {@code null} si no se encuentra.
     */
    private String extraerValor(Map<String, Answer> answers, String key) {
        Object obj = answers.values().stream()
                .filter(a -> ((Map<?, ?>) a).containsValue(key))
                .findFirst()
                .orElse(null);
        return obj != null ? ((Map<?, ?>) obj).get("textAnswers").toString() : null;
    }
    /**
     * Crea un formulario de preinscripción en Google Forms con los campos requeridos.
     *
     * @param periodo   el período académico para el cual se genera el formulario
     * @param programas la lista de programas académicos disponibles
     * @param electivas la lista de electivas disponibles
     * @return la URL pública del formulario creado
     */
    public String crearFormulario(PeriodoAcademico periodo, List<Programa> programas, List<Electiva> electivas) {
        try {
            // 1️. Crear el formulario con título mínimo
            Form form = new Form();
            Info info = new Info();
            info.setTitle("Preinscripción de Electivas " + periodo.getSemestre());
            form.setInfo(info);

            Form createdForm = formsService.forms().create(form).execute();
            String formId = createdForm.getFormId();

            // 2️. Preparar batchUpdate para descripción y preguntas
            List<Request> requests = new ArrayList<>();

            Info updatedInfo = new Info()
                    .setDescription("Formulario de preinscripción para el período " + periodo.getSemestre());
            requests.add(new Request().setUpdateFormInfo(
                    new UpdateFormInfoRequest()
                            .setInfo(updatedInfo)
                            .setUpdateMask("description")
            ));

            // Preguntas de texto
            List<String> preguntasTexto = List.of("Correo institucional", "Código del estudiante", "Nombre", "Apellidos");
            int currentIndex = 0;
            for (String pregunta : preguntasTexto) {
                requests.add(new Request().setCreateItem(
                        new CreateItemRequest()
                                .setItem(campoTexto(pregunta))
                                .setLocation(new Location().setIndex(currentIndex++))
                ));
            }

            // Programa académico (combo)
            requests.add(new Request().setCreateItem(
                    new CreateItemRequest()
                            .setItem(campoCombo("Programa académico",
                                    programas.stream().map(Programa::getNombre).toList(),
                                    true))
                            .setLocation(new Location().setIndex(currentIndex++))
            ));

            // Electivas opción 1 a N (según el periodo)
            int numeroOpciones = periodo.getNumeroOpcionesFormulario();

            for (int i = 1; i <= numeroOpciones; i++) {
                boolean obligatorio = (i == 1); // solo la primera es obligatoria
                requests.add(new Request().setCreateItem(
                        new CreateItemRequest()
                                .setItem(campoCombo("Electiva opción " + i,
                                        electivas.stream().map(Electiva::getNombre).toList(),
                                        obligatorio))
                                .setLocation(new Location().setIndex(currentIndex++))
                ));
            }

            // 3️. Ejecutar batchUpdate
            BatchUpdateFormRequest batchRequest = new BatchUpdateFormRequest().setRequests(requests);
            formsService.forms().batchUpdate(formId, batchRequest).execute();

            // 4️. Devolver URL pública
            return "https://docs.google.com/forms/d/" + formId + "/viewform";

        } catch (Exception e) {
            throw new GoogleFormsException("Error al comunicarse con Google Forms", e);
        }
    }

    /**
     * Cierra un formulario de Google Forms para que deje de aceptar respuestas.
     *
     * <p>Utiliza el método {@code forms.setPublishSettings} para actualizar
     * la configuración de publicación del formulario.</p>
     *
     * @param formId ID del formulario de Google Forms.
     * @throws IOException Si ocurre un error al comunicarse con la API.
     */
    public void cerrarFormulario(String formId) {
        try {
        // Crear el estado de publicación
        PublishState publishState = new PublishState()
                .setIsPublished(true).setIsAcceptingResponses(false); // Sigue visible, pero no acepta respuestas

        // Crear el objeto de configuración de publicación
        PublishSettings publishSettings = new PublishSettings()
                .setPublishState(publishState);
        // Construir la solicitud
        SetPublishSettingsRequest request = new SetPublishSettingsRequest()
                .setPublishSettings(publishSettings);

        // Ejecutar la llamada a la API
        formsService.forms().setPublishSettings(formId, request).execute();

        log.info("Formulario [{}] cerrado correctamente. Ya no acepta respuestas.", formId);
        } catch (Exception e) {
            throw new GoogleFormsException("Error al comunicarse con Google Forms", e);
        }
    }
    // --------------------------------------------------------
    // MÉTODOS AUXILIARES
    // --------------------------------------------------------
    // Método helper para preguntas de texto
    private Item campoTexto(String label) {
        return new Item()
                .setTitle(label)
                .setQuestionItem(new QuestionItem()
                        .setQuestion(new Question()
                                .setRequired(true)
                                .setTextQuestion(new TextQuestion().setParagraph(false))
                        )
                );
    }

    // Método helper para preguntas tipo combo (drop-down)
    private Item campoCombo(String label, List<String> opciones, boolean obligatorio) {
        List<Option> options = opciones.stream().map(op -> new Option().setValue(op)).toList();
        return new Item()
                .setTitle(label)
                .setQuestionItem(new QuestionItem()
                        .setQuestion(new Question()
                                .setRequired(obligatorio)
                                .setChoiceQuestion(new ChoiceQuestion()
                                        .setType("DROP_DOWN")
                                        .setOptions(options)
                                )
                        )
                );
    }
}
