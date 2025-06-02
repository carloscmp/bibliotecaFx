package br.com.carlos.bibliotecafx.util;

import br.com.carlos.bibliotecafx.DTO.LivroDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LivroAPI {

    public static List<LivroDTO> buscarLivrosPorTitulo(String titulo) throws IOException {
        String encoded = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + encoded;

        String respostaJson = null;
        try {
            respostaJson = HttpUtil.get(url);
        } catch (Exception ex) {
            Logger.getLogger(LivroAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(respostaJson);

        List<LivroDTO> livros = new ArrayList<>();

        JsonNode items = root.get("items");
        if (items != null && items.isArray()) {
            for (JsonNode item : items) {
                JsonNode volumeInfo = item.get("volumeInfo");

                String tituloLivro = volumeInfo.has("title") ? volumeInfo.get("title").asText() : "Sem título";
                String autor = volumeInfo.has("authors") ? volumeInfo.get("authors").get(0).asText() : "Desconhecido";
                String sinopse = volumeInfo.has("description") ? volumeInfo.get("description").asText() : null;

                Integer ano = null;
                if (volumeInfo.has("publishedDate")) {
                    String published = volumeInfo.get("publishedDate").asText();
                    if (published.matches("\\d{4}.*")) {
                        try {
                            ano = Integer.parseInt(published.substring(0, 4));
                        } catch (NumberFormatException e) {
                            ano = null;
                        }
                    }
                }

                Integer numeroPaginas = null;
                if (volumeInfo.has("pageCount") && volumeInfo.get("pageCount").isInt()) {
                    numeroPaginas = volumeInfo.get("pageCount").asInt();
                }

                // 🔽 Extrair a URL da capa
                String capaUrl = null;
                if (volumeInfo.has("imageLinks") && volumeInfo.get("imageLinks").has("thumbnail")) {
                    capaUrl = volumeInfo.get("imageLinks").get("thumbnail").asText();
                    // Optional: mudar http -> https (caso necessário)
                    if (capaUrl.startsWith("http:")) {
                        capaUrl = capaUrl.replace("http:", "https:");
                    }
                }

                LivroDTO dto = new LivroDTO();
                dto.setTitulo(tituloLivro);
                dto.setAutor(autor);
                dto.setSinopse(sinopse);
                dto.setAno(ano);
                dto.setNumeroPaginas(numeroPaginas);
                dto.setCapaUrl(capaUrl); // ✅ aqui

                livros.add(dto);
            }
        }

        return livros;
    }
}
