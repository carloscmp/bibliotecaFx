package br.com.carlos.bibliotecafx.service;

import br.com.carlos.bibliotecafx.model.AcaoPendente;
import br.com.carlos.bibliotecafx.model.LivroFx;
import br.com.carlos.bibliotecafx.util.ConfigUtil;
import br.com.carlos.bibliotecafx.util.FilaSincronizacao;
import br.com.carlos.bibliotecafx.util.GerenciadorDadosLocal;
import br.com.carlos.bibliotecafx.util.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServicoSincronizacao implements Runnable {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void run() {
        System.out.println("SYNC: Verificando ações pendentes...");
        List<AcaoPendente> acoes = FilaSincronizacao.getAcoes();
        if (acoes.isEmpty()) {
            System.out.println("SYNC: Nenhuma ação pendente.");
            return;
        }

        List<AcaoPendente> acoesConcluidas = new ArrayList<>();
        for (AcaoPendente acao : acoes) {
            try {
                System.out.println("SYNC: Processando ação -> " + acao.getTipo() + " para livro ID: " + acao.getLivroId());
                String urlBase = ConfigUtil.getProperty("server.url");

                if ("ADD".equals(acao.getTipo())) {
                    String jsonParaEnviar = MAPPER.writeValueAsString(acao.getPayload());
                    String respostaJson = HttpUtil.postComRetorno(urlBase, jsonParaEnviar);
                    LivroFx livroSincronizado = MAPPER.readValue(respostaJson, LivroFx.class);
                    atualizarIdLocal(acao.getPayload()
                                         .getId(), livroSincronizado.getId());

                } else if ("UPDATE".equals(acao.getTipo())) {
                    String jsonParaEnviar = MAPPER.writeValueAsString(acao.getPayload());
                    HttpUtil.put(urlBase + "/" + acao.getLivroId(), jsonParaEnviar);

                } else if ("DELETE".equals(acao.getTipo())) {
                    HttpUtil.delete(urlBase + "/" + acao.getLivroId());
                }

                acoesConcluidas.add(acao);
            } catch (Exception e) {
                System.err.println("SYNC: Falha ao sincronizar ação. Tentando novamente mais tarde.");
                e.printStackTrace();
                break;
            }
        }

        if (!acoesConcluidas.isEmpty()) {
            acoes.removeAll(acoesConcluidas);
            FilaSincronizacao.salvarAcoes(acoes);
            System.out.println("SYNC: " + acoesConcluidas.size() + " ações foram sincronizadas com sucesso.");
        }
    }

    private void atualizarIdLocal(long idTemporario, long idRealDoBanco) {
        System.out.println("SYNC: Atualizando ID local de " + idTemporario + " para " + idRealDoBanco);
        List<LivroFx> biblioteca = GerenciadorDadosLocal.carregarBiblioteca();
        biblioteca.stream()
                  .filter(livro -> Objects.equals(livro.getId(), idTemporario))
                  .findFirst()
                  .ifPresent(livro -> livro.setId(idRealDoBanco));
        GerenciadorDadosLocal.salvarBiblioteca(biblioteca);
    }
}
