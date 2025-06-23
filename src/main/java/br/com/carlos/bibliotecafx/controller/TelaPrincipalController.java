package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.model.LivroFx;
import br.com.carlos.bibliotecafx.util.ConfigUtil;
import br.com.carlos.bibliotecafx.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class TelaPrincipalController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @FXML
    private TableView<LivroFx> tabelaLivros;
    @FXML
    private TableColumn<LivroFx, String> colTitulo;
    @FXML
    private TableColumn<LivroFx, String> colAutor;
    @FXML
    private Label labelTitulo;
    @FXML
    private Label labelAutor;
    @FXML
    private Label labelAno;
    @FXML
    private Label labelPaginas;
    @FXML
    private Label lblSinopse;
    @FXML
    private ImageView imageCapa;
    @FXML
    private Button btnDeletarLivro;

    @FXML
    public void initialize() {
        configurarTabela();
        carregarLivros();
    }

    private void configurarTabela() {
        colTitulo.setCellValueFactory(data -> data.getValue().tituloProperty());
        colAutor.setCellValueFactory(data -> data.getValue().autorProperty());

        tabelaLivros.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> {
                    exibirDetalhesLivro(novo);
                    // Habilita/desabilita o botão de deletar com base na seleção
                    if (btnDeletarLivro != null) {
                        btnDeletarLivro.setDisable(novo == null);
                    }
                }
        );

        tabelaLivros.setPlaceholder(new Label("Carregando livros..."));
    }

    @FXML
    private void carregarLivros() {
        Task<List<LivroFx>> carregarTask = new Task<>() {
            @Override
            protected List<LivroFx> call() throws Exception {
                String url = ConfigUtil.getProperty("server.url");
                String json = HttpUtil.get(url);
                return MAPPER.readValue(json, new TypeReference<>() {
                });
            }
        };

        carregarTask.setOnSucceeded(event -> {
            tabelaLivros.setItems(FXCollections.observableArrayList(carregarTask.getValue()));
            if (tabelaLivros.getItems().isEmpty()) {
                tabelaLivros.setPlaceholder(new Label("Nenhum livro na estante."));
            }
        });

        carregarTask.setOnFailed(event -> {
            tabelaLivros.setPlaceholder(new Label("Falha ao carregar livros."));
            Throwable ex = carregarTask.getException();
            ex.printStackTrace();
            mostrarAlertaErro("Erro de Rede", "Não foi possível buscar os livros do servidor.", ex.getMessage());
        });

        new Thread(carregarTask).start();
    }

    private void exibirDetalhesLivro(LivroFx livro) {
        boolean livroNaoSelecionado = (livro == null);

        labelTitulo.setText(livroNaoSelecionado ? "" : livro.getTitulo());
        labelAutor.setText(livroNaoSelecionado ? "" : livro.getAutor());
        labelAno.setText(livroNaoSelecionado ? "" : String.valueOf(livro.getAno()));
        labelPaginas.setText(livroNaoSelecionado ? "" : String.valueOf(livro.getNumeroPaginas()));
        lblSinopse.setText(livroNaoSelecionado ? "" : livro.getSinopse());

        if (!livroNaoSelecionado && livro.getCapa() != null && livro.getCapa().length > 0) {
            Image imagem = new Image(new java.io.ByteArrayInputStream(livro.getCapa()));
            imageCapa.setImage(imagem);
        } else {
            imageCapa.setImage(null);
        }
    }

    /**
     * Método chamado pelo onAction do botão "Deletar Livro". Realiza a exclusão
     * do livro selecionado após confirmação do usuário.
     */
    @FXML
    private void deletarLivro() {
        LivroFx livroSelecionado = tabelaLivros.getSelectionModel().getSelectedItem();

        if (livroSelecionado == null) {
            mostrarAlertaErro("Nenhum Livro Selecionado", "Por favor, selecione um livro na lista para excluir.", "");
            return;
        }

        // 1. Criar e exibir um diálogo de confirmação
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Você tem certeza que deseja excluir este livro?");
        confirmacao.setContentText("Livro: " + livroSelecionado.getTitulo() + "\nAutor: " + livroSelecionado.getAutor());

        Optional<ButtonType> resultado = confirmacao.showAndWait();

        // 2. Se o usuário confirmar (clicar em OK), prosseguir com a exclusão
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // 3. Executar a chamada de rede em uma tarefa de segundo plano
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    // Assumindo que LivroFx tem o método getId()
                    Long id = livroSelecionado.getId();
                    String urlBase = ConfigUtil.getProperty("server.url");
                    String urlDelecao = urlBase + "/" + id;

                    HttpUtil.delete(urlDelecao);
                    return null;
                }
            };

            deleteTask.setOnSucceeded(event -> {
                // 4. Em caso de sucesso, limpar a seleção e recarregar a lista
                tabelaLivros.getSelectionModel().clearSelection();
                carregarLivros();
            });

            deleteTask.setOnFailed(event -> {
                Throwable ex = deleteTask.getException();
                ex.printStackTrace();
                mostrarAlertaErro("Erro ao Excluir", "Ocorreu uma falha ao tentar excluir o livro.", ex.getMessage());
            });

            new Thread(deleteTask).start();
        }
    }

    @FXML
    public void abrirTelaBusca() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ConfigUtil.getProperty("fxml.path.busca")));
            Parent root = loader.load();

            TelaBuscaLivroController buscaController = loader.getController();
            buscaController.setOnBuscaConcluidaCallback(this::carregarLivros);

            Stage stage = new Stage();
            stage.setTitle("Adicionar/Buscar Livro");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaErro("Erro de Interface", "Ocorreu um erro inesperado ao carregar a tela de busca.", e.getMessage());
        }
    }

    private void mostrarAlertaErro(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}
