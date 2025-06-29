package br.com.carlos.bibliotecafx.util;

import br.com.carlos.bibliotecafx.DTO.LivroDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LivroAPI {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String API_KEY = ConfigUtil.getProperty("google.api.key", "");

    public static List<LivroDTO> buscarLivrosPorTitulo(String titulo) throws IOException, InterruptedException {
        String baseUrl = ConfigUtil.getProperty("google.books.api.url");
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IOException("A URL da API do Google Books não está configurada em config.properties.");
        }

        String encodedTitulo = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
        String url = String.format("%s?q=%s&maxResults=20&key=%s", baseUrl, encodedTitulo, API_KEY);

        String respostaJson = HttpUtil.get(url);

        GoogleBooksResponse response = MAPPER.readValue(respostaJson, GoogleBooksResponse.class);

        List<LivroDTO> livros = new ArrayList<>();
        if (response.items == null) {
            return livros;
        }

        for (VolumeItem item : response.items) {
            if (item.volumeInfo == null) {
                continue;
            }

            VolumeInfo volumeInfo = item.volumeInfo;
            LivroDTO dto = new LivroDTO();

            dto.setTitulo(volumeInfo.title != null ? volumeInfo.title : "Sem título");

            if (volumeInfo.authors != null && !volumeInfo.authors.isEmpty()) {
                dto.setAutor(String.join(", ", volumeInfo.authors));
            } else {
                dto.setAutor("Autor desconhecido");
            }

            dto.setSinopse(volumeInfo.description);
            dto.setNumeroPaginas(volumeInfo.pageCount);

            if (volumeInfo.publishedDate != null && volumeInfo.publishedDate.matches("\\d{4}.*")) {
                try {
                    dto.setAno(Integer.parseInt(volumeInfo.publishedDate.substring(0, 4)));
                } catch (NumberFormatException e) {
                }
            }

            if (volumeInfo.imageLinks != null && volumeInfo.imageLinks.thumbnail != null) {
                String capaUrl = volumeInfo.imageLinks.thumbnail.replace("http:", "https:");
                dto.setCapaUrl(capaUrl);
            }

            livros.add(dto);
        }
        return livros;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleBooksResponse {

        public List<VolumeItem> items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeItem {

        public VolumeInfo volumeInfo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeInfo {

        public String title;
        public List<String> authors;
        public String description;
        public String publishedDate;
        public Integer pageCount;
        public ImageLinks imageLinks;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageLinks {

        public String thumbnail;
        public String smallThumbnail;
    }
}
