package com.unicauca.fiet.sistema_electivas.integracion.google;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.model.*;
import com.google.api.client.auth.oauth2.Credential;

import com.unicauca.fiet.sistema_electivas.common.exception.GoogleFormsException;
import com.unicauca.fiet.sistema_electivas.electiva.model.Electiva;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.PeriodoAcademico;
import com.unicauca.fiet.sistema_electivas.periodo_academico.model.RespuestasFormulario;
import com.unicauca.fiet.sistema_electivas.programa.model.Programa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;
import java.net.URL;


/**
 * Cliente para la integración con la API de Google Forms.
 *
 * <p>Permite crear, consultar y cerrar formularios de Google Forms desde el sistema académico.
 * Su función principal es abstraer la comunicación con la API de Google, proporcionando
 * operaciones de alto nivel reutilizables por los servicios del dominio {@code periodoacademico}.</p>
 *
 * <p>Acciones soportadas:
 * <ul>
 * <li>Creación dinámica de formularios de preinscripción de electivas.</li>
 * <li>Consulta y conversión de respuestas recibidas en estructuras manipulables.</li>
 * <li>Cierre de formularios una vez finalizado el período de inscripción.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleFormsClient {
    /** Cliente oficial de la API de Google Forms, inyectado mediante configuración de seguridad OAuth. */
    private final Forms formsService;
    private final Credential googleCredential;

    private final String appsScriptUrl = "https://script.googleapis.com/v1/scripts/AKfycbwRxkD8ipBGCZCk7_DJ8u12w6avpESkjHTUhOB4ybhSywTfQTShsTBjrVibxiHoVJ06jA:run";

    private final ObjectMapper objectMapper = new ObjectMapper();
    // --------------------------------------------------------
    // MÉTODOS PRINCIPALES DE INTEGRACIÓN
    // --------------------------------------------------------

    /**
     * Obtiene todas las respuestas enviadas a un formulario de Google Forms y las devuelve como respuesta.
     *
     * <p>Acciones realizadas:
     * <ul>
     * <li>Consulta la API de Google Forms para obtener las respuestas.</li>
     * <li>Mapea cada respuesta a una entidad {@link RespuestasFormulario}.</li>
     * <li>Devuelve todas las respuestas en la base de datos asociadas al período correspondiente.</li>
     * </ul>
     *
     * @param formId  ID del formulario de Google Forms.
     * @throws RuntimeException Si ocurre un error al obtener o procesar las respuestas.
     */
    public List<Map<String, String>> obtenerRespuestas(String formId) {
        try {
            //  Obtener estructura del formulario
            Form form = formsService.forms().get(formId).execute();

            //  Construir mapa de questionId -> título de la pregunta
            Map<String, String> mapaPreguntas = new HashMap<>();
            for (Item item : form.getItems()) {
                if (item.getQuestionItem() != null && item.getQuestionItem().getQuestion() != null) {
                    mapaPreguntas.put(item.getQuestionItem().getQuestion().getQuestionId(), item.getTitle());
                }
            }
            // LISTA FINAL COMPLETA
            List<Map<String, String>> respuestasTotales = new ArrayList<>();

            // ------ PAGINACIÓN ------
            String nextPageToken = null;
            //  Obtener respuestas
            do {
                ListFormResponsesResponse response =
                        formsService.forms()
                                .responses()
                                .list(formId)
                                .setPageToken(nextPageToken) // <--- clave
                                .execute();

                if (response.getResponses() != null) {
                    for (FormResponse fr : response.getResponses()) {
                        respuestasTotales.add(convertirAmapa(fr, mapaPreguntas));
                    }
                }

                nextPageToken = response.getNextPageToken(); // <--- ¿hay más páginas?

            } while (nextPageToken != null);

            return respuestasTotales;

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
            fr.getAnswers().forEach((questionId, answer) -> {
                String pregunta = mapaPreguntas.get(questionId);

                // CORRECCIÓN: Validación defensiva para evitar IndexOutOfBoundsException
                String respuestaTexto = "";
                if (answer.getTextAnswers() != null
                        && answer.getTextAnswers().getAnswers() != null
                        && !answer.getTextAnswers().getAnswers().isEmpty()) {

                    respuestaTexto = answer.getTextAnswers().getAnswers().get(0).getValue();
                }

                // Solo agregamos si tenemos la pregunta mapeada
                if (pregunta != null) {
                    mapa.put(pregunta, respuestaTexto);
                }
            });
        }

        // 👇 Agregamos la fecha real del envío del formulario
        mapa.put("timestampRespuesta", fr.getLastSubmittedTime());

        // Agregamos el email si está disponible (a veces viene fuera de las respuestas si se recolecta automáticamente)
        if (fr.getRespondentEmail() != null) {
            // Usamos la clave que espera tu parser: "Correo institucional"
            // OJO: Si ya existe una pregunta con este nombre, esto podría sobrescribirla o duplicarla
            // Es mejor verificar si ya vino en las respuestas.
            mapa.putIfAbsent("Correo institucional", fr.getRespondentEmail());
        }

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

    public Map<String, Object> generarFormulario(
            PeriodoAcademico periodo,
            List<Programa> programas,
            Map<Long, List<Electiva>> electivasPorPrograma
    ) {
        try {
            // Convertir electivasPorPrograma a Map<String, List<String>>
            Map<String, List<String>> electivasMap = new HashMap<>();
            for (var entry : electivasPorPrograma.entrySet()) {
                electivasMap.put(
                        entry.getKey().toString(),
                        entry.getValue().stream().map(Electiva::getNombre).toList()
                );
            }

            // Crear JSON para Apps Script
            Map<String, Object> payload = Map.of(
                    "function", "generarFormulario",
                    "parameters", List.of(
                            Map.of(
                                    "periodo", periodo.getSemestre(),
                                    "programas", programas.stream().map(p -> Map.of(
                                            "id", p.getId(),
                                            "nombre", p.getNombre(),
                                            "opciones", periodo.getOpcionesPorPrograma().get(p.getId())
                                    )).toList(),
                                    "electivas", electivasMap
                            )
                    )
            );


            String token = googleCredential.getAccessToken();

            HttpURLConnection con = (HttpURLConnection) new URL(appsScriptUrl).openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + token);


            // Escribir JSON
            try (OutputStream os = con.getOutputStream()) {
                os.write(objectMapper.writeValueAsBytes(payload));
            }

            int status = con.getResponseCode();

            // Si Apps Script devolvió error 4xx o 5xx → leer el error
            if (status >= 400) {
                String errorBody = new BufferedReader(new InputStreamReader(con.getErrorStream()))
                        .lines().collect(Collectors.joining());

                System.err.println("ERROR HTTP " + status);
                System.err.println("CUERPO DEVUELTO POR APPS SCRIPT:");
                System.err.println(errorBody);

                throw new RuntimeException("Error HTTP " + status + ": " + errorBody);
            }

            // Leer respuesta OK
            String response = new BufferedReader(new InputStreamReader(con.getInputStream()))
                    .lines().collect(Collectors.joining());

            Map<String, Object> root = objectMapper.readValue(response, Map.class);

            Map<String, Object> resp = (Map<String, Object>) root.get("response");

            if (resp == null || resp.get("result") == null) {
                throw new RuntimeException("Apps Script no devolvió 'response.result'. Respuesta cruda: " + response);
            }

            return (Map<String, Object>) resp.get("result");

        } catch (Exception e) {
            System.err.println("EXCEPCIÓN COMPLETA EN generarFormulario():");
            e.printStackTrace(); // stacktrace detallado

            throw new RuntimeException("Error llamando a Apps Script", e);
        }
    }

    /**
     * Cierra un formulario de Google Forms para que deje de aceptar respuestas.
     *
     * <p>Utiliza el método {@code forms.setPublishSettings} para actualizar
     * la configuración de publicación del formulario.</p>
     *
     * @param formId ID del formulario de Google Forms.
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
            // 🔥 1. Imprimir el mensaje base
            log.error("❌ Error al comunicarse con Google Forms: {}", e.getMessage());

            // 🔥 2. Imprimir todo el cuerpo de respuesta de la API (si es un HttpResponseException)
            if (e instanceof com.google.api.client.http.HttpResponseException httpError) {
                log.error("❌ Código HTTP: {}", httpError.getStatusCode());
                log.error("❌ Respuesta API: {}", httpError.getContent());
            }

            // 🔥 3. Lanzar tu excepción como antes
            throw new GoogleFormsException("Error al comunicarse con Google Forms: " + e.getMessage(), e);
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

    private Item campoComboProgramaConSaltos(List<Programa> programas) {
        List<Option> opciones = new ArrayList<>();
        for (Programa p : programas) {
            opciones.add(new Option().setValue(p.getNombre())); // saltos se agregan luego
        }

        return new Item()
                .setTitle("Programa académico")
                .setQuestionItem(new QuestionItem()
                        .setQuestion(new Question()
                                .setRequired(true)
                                .setChoiceQuestion(new ChoiceQuestion()
                                        .setType("DROP_DOWN")
                                        .setOptions(opciones)
                                )
                        )
                );
    }

    private List<Request> crearSaltosDePrograma(List<Programa> programas, Map<Long, String> seccionPorPrograma, String programaItemId, int indicePrograma) {
        List<Request> requests = new ArrayList<>();

        List<Option> opcionesActualizadas = new ArrayList<>();
        for (Programa p : programas) {
            opcionesActualizadas.add(
                    new Option()
                            .setValue(p.getNombre())
                            .setGoToSectionId(seccionPorPrograma.get(p.getId()))
            );
        }

        requests.add(new Request().setUpdateItem(
                new UpdateItemRequest()
                        .setItem(new Item()
                                .setItemId(programaItemId) // <- indicamos qué item actualizar
                                .setQuestionItem(new QuestionItem()
                                        .setQuestion(new Question()
                                                .setChoiceQuestion(new ChoiceQuestion()
                                                        .setType("DROP_DOWN")
                                                        .setOptions(opcionesActualizadas)
                                                )
                                        )
                                )
                        )
                        .setLocation(new Location().setIndex(indicePrograma)) // ← REQUERIDO
                        .setUpdateMask("questionItem.question.choiceQuestion.options")
        ));

        return requests;
    }

}