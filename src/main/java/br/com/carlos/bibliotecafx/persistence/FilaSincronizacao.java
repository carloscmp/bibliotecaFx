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

    /**
     * Lê a lista de ações pendentes do arquivo JSON.
     * O método é 'synchronized' para garantir a segurança em ambientes com múltiplas threads.
     *
     * @return A lista de ações pendentes.
     */
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

    /**
     * Salva a lista de ações pendentes no arquivo JSON.
     * O método é 'synchronized' para segurança.
     *
     * @param acoes A lista de ações a ser salva.
     */
    public static synchronized void salvarAcoes(List<AcaoPendente> acoes) {
        try {
            MAPPER.writeValue(CAMINHO_FILA.toFile(), acoes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adiciona uma nova ação à fila de forma inteligente.
     * Se a ação for um UPDATE ou DELETE para um livro que acabou de ser adicionado (ID negativo),
     * ele modifica a ação ADD existente em vez de criar uma nova.
     *
     * @param novaAcao A nova ação a ser processada.
     */
    public static synchronized void adicionarOuAtualizarAcao(AcaoPendente novaAcao) {
        List<AcaoPendente> acoes = getAcoes();
        boolean acaoFundida = false;

        Long idAlvo = novaAcao.getPayload() != null ? novaAcao.getPayload()
                                                              .getId() : novaAcao.getLivroId();

        // Se a nova ação é para um livro com ID temporário (negativo)...
        if (idAlvo != null && idAlvo < 0) {
            // ...procura na fila se já existe uma ação ADD para este mesmo ID.
            for (int i = 0; i < acoes.size(); i++) {
                AcaoPendente acaoExistente = acoes.get(i);
                if ("ADD".equals(acaoExistente.getTipo()) && acaoExistente.getPayload() != null && Objects.equals(acaoExistente.getPayload()
                                                                                                                               .getId(), idAlvo)) {

                    if ("UPDATE".equals(novaAcao.getTipo())) {
                        // Se encontrou, atualiza o payload da ação ADD com os novos dados
                        System.out.println("SYNC: Ação UPDATE fundida com a ação ADD existente para o ID temporário: " + idAlvo);
                        acaoExistente.setPayload(novaAcao.getPayload());
                    } else if ("DELETE".equals(novaAcao.getTipo())) {
                        // Se a ação é deletar um livro que nem foi pro servidor ainda, simplesmente remove a ação ADD
                        System.out.println("SYNC: Ação DELETE cancelou a ação ADD pendente para o ID temporário: " + idAlvo);
                        acoes.remove(i);
                    }
                    acaoFundida = true;
                    break;
                }
            }
        }

        // Se nenhuma ação foi fundida (ou se o ID é positivo), apenas adiciona a nova ação no final da fila
        if (!acaoFundida) {
            acoes.add(novaAcao);
        }

        salvarAcoes(acoes);
    }
}
