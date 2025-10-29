package com.unicauca.fiet.sistema_electivas.integracion.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.forms.v1.Forms;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStreamReader;
import java.util.List;
/**
 * Configuración de autenticación y cliente de Google Forms.
 *
 * <p>Esta clase define un {@link Forms} bean autenticado que permite a la aplicación
 * interactuar con la API de Google Forms y Google Drive utilizando OAuth 2.0.</p>
 *
 * <p>Acciones realizadas:
 * <ul>
 *   <li>Carga el archivo de credenciales <b>client_secret.json</b> desde el classpath.</li>
 *   <li>Configura el flujo de autorización OAuth2 para obtener el token de acceso.</li>
 *   <li>Levanta un servidor local para completar el flujo de autenticación.</li>
 *   <li>Devuelve un cliente {@link Forms} completamente autenticado para uso de la aplicación.</li>
 * </ul>
 * </p>
 *
 * <p>Este bean se inyecta automáticamente en componentes como {@link com.tuapp.academico.periodoacademico.infrastructure.GoogleFormsClient}
 * mediante Spring Dependency Injection.</p>
 *
 * @author Juan
 * @since 1.0
 */
@Configuration
public class GoogleFormsConfig {

    private static final String APPLICATION_NAME = "Sistema Electivas";
    /**
     * Crea e inicializa un cliente autenticado de Google Forms API.
     *
     * <p>El proceso sigue estos pasos:</p>
     * <ol>
     *   <li>Carga el archivo de credenciales OAuth 2.0 desde el directorio {@code /resources}.</li>
     *   <li>Configura el flujo de autorización con los scopes de acceso a Forms y Drive.</li>
     *   <li>Levanta un receptor local en el puerto 8888 para manejar la devolución del código OAuth.</li>
     *   <li>Solicita autorización al usuario mediante el navegador predeterminado.</li>
     *   <li>Devuelve una instancia autenticada de {@link Forms} lista para usar.</li>
     * </ol>
     *
     * @return Cliente autenticado de Google Forms API.
     * @throws Exception Si ocurre un error al crear el flujo de autenticación o cargar las credenciales.
     */
    @Bean
    public Forms googleFormsService() throws Exception {
        // 1️. Cargar client_secret.json desde resources
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(),
                new InputStreamReader(
                        GoogleFormsConfig.class.getResourceAsStream("/client_secret_475492352745-1a78a31v9etga3mv6qt633mn4ibab5gf.apps.googleusercontent.com.json")
                )
        );

        // 2️. Configurar flujo OAuth
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                clientSecrets,
                List.of(
                        "https://www.googleapis.com/auth/forms",
                        "https://www.googleapis.com/auth/drive"
                )
        ).setAccessType("offline").build();

        // 3️. Recibir autorización del usuario (abre navegador)
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .setCallbackPath("/Callback")
                .build();


        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                .authorize("user");

        // 4️. Crear y devolver servicio autenticado
        return new Forms.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName(APPLICATION_NAME).build();
    }
}
