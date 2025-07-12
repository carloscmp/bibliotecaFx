package br.com.carlos.bibliotecafx.persistence;

import br.com.carlos.bibliotecafx.model.AcaoPendente;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilaSincronizacao {
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final Path CAMINHO_FILA = AppDataUtil.getAppDataDirectory()
                                                        .resolve("acoes_pendentes.json");

    public static synchronized List<AcaoPendente> getAcoes() {
        if (!Files.exists(CAMINHO_FILA)) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(CAMINHO_FILA.toFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static synchronized void salvarAcoes(List<AcaoPendente> acoes) {
        try {
            MAPPER.writeValue(CAMINHO_FILA.toFile(), acoes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void adicionarOuAtualizarAcao(AcaoPendente novaAcao) {
        List<AcaoPendente> acoes = getAcoes();
        boolean acaoFundida = false;

        Long idAlvo = novaAcao.getPayload() != null ? novaAcao.getPayload()
                                                              .getId() : novaAcao.getLivroId();

        if (idAlvo != null && idAlvo < 0) {
            for (int i = 0; i < acoes.size(); i++) {
                AcaoPendente acaoExistente = acoes.get(i);
                if ("ADD".equals(acaoExistente.getTipo()) && acaoExistente.getPayload() != null && Objects.equals(acaoExistente.getPayload()
                                                                                                                               .getId(), idAlvo)) {

                    if ("UPDATE".equals(novaAcao.getTipo())) {
                        System.out.println("SYNC: Ação UPDATE fundida com a ação ADD existente para o ID temporário: " + idAlvo);
                        acaoExistente.setPayload(novaAcao.getPayload());
                    } else if ("DELETE".equals(novaAcao.getTipo())) {
                        System.out.println("SYNC: Ação DELETE cancelou a ação ADD pendente para o ID temporário: " + idAlvo);
                        acoes.remove(i);
                    }
                    acaoFundida = true;
                    break;
                }
            }
        }

        if (!acaoFundida) {
            acoes.add(novaAcao);
        }

        salvarAcoes(acoes);
    }
}
