package br.com.carlos.bibliotecafx.api.googlebooks;

import br.com.carlos.bibliotecafx.DTO.LivroDTO;
import br.com.carlos.bibliotecafx.api.googlebooks.model.GoogleBooksResponse;
import br.com.carlos.bibliotecafx.api.googlebooks.model.VolumeInfo;
import br.com.carlos.bibliotecafx.api.googlebooks.model.VolumeItem;
import br.com.carlos.bibliotecafx.config.ConfigUtil;
import br.com.carlos.bibliotecafx.http.HttpUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LivroAPI {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static List<LivroDTO> buscarLivrosPorTitulo(String titulo) throws IOException, InterruptedException {
        String urlBaseBackend = ConfigUtil.getProperty("server.url");
        if (urlBaseBackend == null || urlBaseBackend.isBlank()) {
            throw new IOException("A URL do servidor backend não está configurada.");
        }

        String encodedTitulo = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
        String urlBuscaSegura = String.format("%s/busca-externa?titulo=%s", urlBaseBackend, encodedTitulo);


        String respostaJson = HttpUtil.get(urlBuscaSegura);

        GoogleBooksResponse response = MAPPER.readValue(respostaJson, GoogleBooksResponse.class);

        List<LivroDTO> livros = new ArrayList<>();
        if (response == null || response.items == null) {
            return livros;
        }

        for (VolumeItem item : response.items) {
            if (item.volumeInfo == null) continue;

            VolumeInfo volumeInfo = item.volumeInfo;
            LivroDTO dto = new LivroDTO();
            dto.setTitulo(volumeInfo.title != null ? volumeInfo.title : "Sem título");
            dto.setAutor(volumeInfo.authors != null ? String.join(", ", volumeInfo.authors) : "Autor desconhecido");
            dto.setSinopse(volumeInfo.description);
            dto.setNumeroPaginas(volumeInfo.pageCount);

            if (volumeInfo.publishedDate != null && volumeInfo.publishedDate.matches("\\d{4}.*")) {
                try {
                    dto.setAno(Integer.parseInt(volumeInfo.publishedDate.substring(0, 4)));
                } catch (NumberFormatException e) { /* ignora */ }
            }

            if (volumeInfo.imageLinks != null && volumeInfo.imageLinks.thumbnail != null) {
                dto.setCapaUrl(volumeInfo.imageLinks.thumbnail.replace("http:", "https:"));
            }
            livros.add(dto);
        }
        return livros;
    }
}