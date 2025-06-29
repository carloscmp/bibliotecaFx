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
import java.util.List;
import java.util.Optional;
import javafx.geometry.Bounds;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class TelaPrincipalController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @FXML
    private VBox containerDetalhes;

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
    private Button btnEditarLivro;

    @FXML
    public void initialize() {
        configurarTabela();
        carregarLivros();
    }

    private void configurarTabela() {
        colTitulo.setCellValueFactory(data -> data.getValue().
                tituloProperty());
        colAutor.setCellValueFactory(data -> data.getValue().
                autorProperty());

        tabelaLivros.getSelectionModel().
                selectedItemProperty().
                addListener(
                        (obs, antigo, novo) -> {
                            exibirDetalhesLivro(novo);
                            boolean desabilitar = (novo == null);
                            btnDeletarLivro.setDisable(desabilitar);
                            btnEditarLivro.setDisable(desabilitar);
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
            tabelaLivros.setItems(FXCollections.observableArrayList(
                    carregarTask.getValue()));
            if(tabelaLivros.getItems().
                    isEmpty()) {
                tabelaLivros.setPlaceholder(
                        new Label("Nenhum livro na estante."));
            }
        });

        carregarTask.setOnFailed(event -> {
            tabelaLivros.setPlaceholder(new Label("Falha ao carregar livros."));
            Throwable ex = carregarTask.getException();
            ex.printStackTrace();
            mostrarAlertaErro(
                    "Erro de Rede",
                    "Não foi possível buscar os livros do servidor.",
                    ex.getMessage());
        });

        new Thread(carregarTask).start();
    }

    private void exibirDetalhesLivro(LivroFx livro) {
        boolean livroNaoSelecionado = (livro == null);

        labelTitulo.setText(livroNaoSelecionado ? "" : livro.getTitulo());
        labelAutor.setText(livroNaoSelecionado ? "" : livro.getAutor());
        labelAno.setText(livroNaoSelecionado ? "" : String.valueOf(livro.
                getAno()));
        labelPaginas.setText(livroNaoSelecionado ? "" : String.valueOf(livro.
                getNumeroPaginas()));
        lblSinopse.setText(livroNaoSelecionado ? "" : livro.getSinopse());

        if(!livroNaoSelecionado && livro.getCapa() != null
                && livro.getCapa().length > 0) {
            Image imagem = new Image(new java.io.ByteArrayInputStream(livro.
                    getCapa()));
            imageCapa.setImage(imagem);
        } else {
            imageCapa.setImage(null);
        }
    }

    @FXML
    private void deletarLivro() {
        LivroFx livroSelecionado = tabelaLivros.getSelectionModel().
                getSelectedItem();

        if(livroSelecionado == null) {
            mostrarAlertaErro("Nenhum Livro Selecionado",
                    "Por favor, selecione um livro na lista para excluir.", "");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText(
                "Você tem certeza que deseja excluir este livro?");
        confirmacao.setContentText("Livro: " + livroSelecionado.getTitulo()
                + "\nAutor: " + livroSelecionado.getAutor());

        Optional<ButtonType> resultado = confirmacao.showAndWait();

        if(resultado.isPresent() && resultado.get() == ButtonType.OK) {
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Long id = livroSelecionado.getId();
                    String urlBase = ConfigUtil.getProperty("server.url");
                    String urlDelecao = urlBase + "/" + id;

                    HttpUtil.delete(urlDelecao);
                    return null;
                }
            };

            deleteTask.setOnSucceeded(event -> {
                tabelaLivros.getSelectionModel().
                        clearSelection();
                carregarLivros();
            });

            deleteTask.setOnFailed(event -> {
                Throwable ex = deleteTask.getException();
                ex.printStackTrace();
                mostrarAlertaErro("Erro ao Excluir",
                        "Ocorreu uma falha ao tentar excluir o livro.", ex.
                                getMessage());
            });

            new Thread(deleteTask).start();
        }
    }

    @FXML
    private void editarLivroSelecionado() {
        LivroFx livroSelecionado = tabelaLivros.getSelectionModel().
                getSelectedItem();
        if(livroSelecionado == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().
                    getResource(ConfigUtil.getProperty("fxml.path.edicao")));
            Parent root = loader.load();

            Bounds boundsNaTela = containerDetalhes.localToScreen(
                    containerDetalhes.getBoundsInLocal());
            double x = boundsNaTela.getMinX();
            double y = boundsNaTela.getMinY();
            double largura = containerDetalhes.getWidth();
            double altura = containerDetalhes.getHeight();

            Stage edicaoStage = new Stage();
            edicaoStage.setTitle("Editando: " + livroSelecionado.getTitulo());
            edicaoStage.initModality(Modality.WINDOW_MODAL);
            edicaoStage.initOwner(tabelaLivros.getScene().
                    getWindow());
            Scene scene = new Scene(root, largura, altura);
            edicaoStage.setScene(scene);

            edicaoStage.setX(x);
            edicaoStage.setY(y);

            TelaEdicaoLivroController controller = loader.getController();
            controller.inicializarDados(livroSelecionado);
            controller.setOnEdicaoConcluidaCallback(this::carregarLivros);
            edicaoStage.showAndWait();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    // Dentro da classe TelaPrincipalController.java
    /**
     * Chamado pelo MenuItem "Buscar Online". Abre a tela de busca na API do
     * Google Books.
     */
    @FXML
    private void abrirTelaBuscaOnline() {
        try {
            String fxmlPath = ConfigUtil.getProperty("fxml.path.busca");
            if(fxmlPath == null || fxmlPath.isBlank()) {
                mostrarAlertaErro("Erro de Configuração",
                        "Caminho para a tela de busca online não encontrado em config.properties.",
                        "Verifique a chave 'fxml.path.busca'");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().
                    getResource(fxmlPath));
            Parent root = loader.load();

            // Configura o callback para atualizar a lista principal quando a busca terminar
            TelaBuscaLivroController buscaController = loader.getController();
            buscaController.setOnBuscaConcluidaCallback(this::carregarLivros);

            Stage stage = new Stage();
            stage.setTitle("Buscar Livro Online");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaLivros.getScene().
                    getWindow());
            stage.showAndWait();

        } catch(Exception e) {
            e.printStackTrace();
            mostrarAlertaErro("Erro de Interface",
                    "Ocorreu um erro ao carregar a tela de busca online.", e.
                            getMessage());
        }
    }

    /**
     * Chamado pelo MenuItem "Cadastro Manual". Abre a tela de formulário para
     * adicionar um novo livro do zero.
     */
    @FXML
    private void abrirTelaCadastroManual() {
        try {
            // Reutilizamos a tela de edição, que agora também é de cadastro
            String fxmlPath = ConfigUtil.getProperty("fxml.path.edicao");
            if(fxmlPath == null || fxmlPath.isBlank()) {
                mostrarAlertaErro("Erro de Configuração",
                        "Caminho para a tela de cadastro/edição não encontrado em config.properties.",
                        "Verifique a chave 'fxml.path.edicao'");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().
                    getResource(fxmlPath));
            Parent root = loader.load();

            TelaEdicaoLivroController edicaoController = loader.getController();

            // Abre a tela em "Modo Criação", passando um objeto novo e vazio
            edicaoController.inicializarDados(new LivroFx());

            // Passa o callback para atualizar a tabela principal após o novo livro ser salvo
            edicaoController.setOnEdicaoConcluidaCallback(this::carregarLivros);

            Stage stage = new Stage();
            stage.setTitle("Adicionar Novo Livro Manualmente");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaLivros.getScene().
                    getWindow());
            stage.showAndWait();

        } catch(Exception e) {
            e.printStackTrace();
            mostrarAlertaErro("Erro de Interface",
                    "Ocorreu um erro ao carregar a tela de cadastro manual.", e.
                            getMessage());
        }
    }

// Lembre-se que o método antigo 'abrirTelaBusca' ou 'abrirTelaCadastro' pode ser removido agora.
    private void mostrarAlertaErro(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}
