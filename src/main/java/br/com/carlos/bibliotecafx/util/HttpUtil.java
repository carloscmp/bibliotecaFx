package br.com.carlos.bibliotecafx.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Classe utilitária para realizar chamadas HTTP.
 */
public class HttpUtil {

    // Cria um cliente HTTP reutilizável com configurações padrão.
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10)) // Timeout de 10 segundos para conectar
            .build();

    /**
     * Realiza uma requisição HTTP GET para a URL especificada.
     *
     * @param url A URL para a qual a requisição será feita.
     * @return O corpo da resposta como uma String.
     * @throws IOException Se a requisição falhar, se o status não for 200 (OK),
     * ou se ocorrer um timeout.
     */
    public static String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // Envia a requisição e espera a resposta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Verifica se a requisição foi bem-sucedida
        if (response.statusCode() != 200) {
            throw new IOException("Falha na requisição HTTP: Status code " + response.statusCode());
        }

        return response.body();
    }

    // Adicione aqui o método post se ainda não o tiver, ou o mantenha se já existir.
    public static void post(String url, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) { // 201 = Created
            throw new IOException("Falha na requisição HTTP POST: Status code " + response.statusCode());
        }
    }
}
