package br.com.carlos.bibliotecafx.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Classe utilitária para centralizar as chamadas HTTP da aplicação. Utiliza o
 * HttpClient moderno do Java (padrão desde o Java 11).
 */
public final class HttpUtil {

    // Cria um cliente HTTP reutilizável com configurações padrão.
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10)) // Timeout de 10 segundos para conectar
            .build();

    /**
     * Construtor privado para impedir que a classe seja instanciada.
     */
    private HttpUtil() {
    }

    /**
     * Realiza uma requisição HTTP GET para a URL especificada.
     *
     * @param url A URL para a qual a requisição será feita.
     * @return O corpo da resposta como uma String.
     * @throws IOException Se a requisição falhar, se o status não for de
     * sucesso (2xx), ou se ocorrer um timeout.
     * @throws InterruptedException Se a thread for interrompida durante a
     * espera.
     */
    public static String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        // Envia a requisição e espera a resposta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Verifica se a requisição foi bem-sucedida (qualquer status 2xx)
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha na requisição HTTP GET: Status code " + response.statusCode() + " | Body: " + response.body());
        }

        return response.body();
    }

    /**
     * Realiza uma requisição HTTP POST para a URL especificada com um corpo
     * JSON.
     *
     * @param url A URL para a qual a requisição será feita.
     * @param json O corpo da requisição em formato de String JSON.
     * @throws IOException Se a requisição falhar, se o status não for de
     * sucesso (2xx), ou se ocorrer um timeout.
     * @throws InterruptedException Se a thread for interrompida durante a
     * espera.
     */
    public static void post(String url, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Verifica se a requisição foi bem-sucedida (qualquer status 2xx, como 200 OK ou 201 Created)
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha na requisição HTTP POST: Status code " + response.statusCode() + " | Body: " + response.body());
        }
    }

    /**
     * Realiza uma requisição HTTP DELETE para a URL especificada. Usado para
     * remover um recurso no servidor.
     *
     * @param url A URL do recurso a ser deletado.
     * @throws IOException Se a requisição falhar, se o status não for de
     * sucesso (2xx), ou se ocorrer um timeout.
     * @throws InterruptedException Se a thread for interrompida durante a
     * espera.
     */
    public static void delete(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();

        // Envia a requisição e descarta o corpo da resposta, pois geralmente é vazio (ou não é necessário).
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

        // Verifica se a requisição foi bem-sucedida (qualquer status 2xx, como 200 OK ou 204 No Content)
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha na requisição HTTP DELETE: Status code " + response.statusCode());
        }
    }
}
