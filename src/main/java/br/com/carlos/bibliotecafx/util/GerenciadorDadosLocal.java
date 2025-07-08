package br.com.carlos.bibliotecafx.util;

import br.com.carlos.bibliotecafx.model.LivroFx;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class GerenciadorDadosLocal {
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final Path CAMINHO_BIBLIOTECA = AppDataUtil.getAppDataDirectory()
                                                              .resolve("biblioteca_local.json");

    public static synchronized void salvarBiblioteca(List<LivroFx> livros) {
        try {
            MAPPER.writeValue(CAMINHO_BIBLIOTECA.toFile(), livros);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized List<LivroFx> carregarBiblioteca() {
        if (!Files.exists(CAMINHO_BIBLIOTECA)) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(CAMINHO_BIBLIOTECA.toFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}