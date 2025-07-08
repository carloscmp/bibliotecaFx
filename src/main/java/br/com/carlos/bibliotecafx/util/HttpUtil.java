package br.com.carlos.bibliotecafx.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class HttpUtil {

    private static final HttpClient client = HttpClient.newBuilder()
                                                       .version(HttpClient.Version.HTTP_2)
                                                       .connectTimeout(Duration.ofSeconds(10)) // Timeout de 10 segundos para conectar
                                                       .build();

    private HttpUtil() {
    }

    public static String put(String url, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(url))
                                         .header("Content-Type", "application/json")
                                         .PUT(HttpRequest.BodyPublishers.ofString(json)) // <<< A MUDANÇA ESTÁ AQUI
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha na requisição HTTP PUT: Status code " + response.statusCode() + " | Body: " + response.body());
        }
        return response.body();
    }

    public static String postComRetorno(String url, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(url))
                                         .header("Content-Type", "application/json")
                                         .POST(HttpRequest.BodyPublishers.ofString(json))
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha na requisição HTTP POST: Status code " + response.statusCode() + " | Body: " + response.body());
        }
        return response.body(); // Retorna o JSON da resposta
    }

    public static String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(url))
                                         .header("Accept", "application/json")
                                         .GET()
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha na requisição HTTP GET: Status code " + response.statusCode() + " | Body: " + response.body());
        }

        return response.body();
    }

    public static void post(String url, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(url))
                                         .header("Content-Type", "application/json")
                                         .POST(HttpRequest.BodyPublishers.ofString(json))
                                         .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha na requisição HTTP POST: Status code " + response.statusCode() + " | Body: " + response.body());
        }
    }

    public static void delete(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(url))
                                         .DELETE()
                                         .build();

        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Falha na requisição HTTP DELETE: Status code " + response.statusCode());
        }
    }
}
