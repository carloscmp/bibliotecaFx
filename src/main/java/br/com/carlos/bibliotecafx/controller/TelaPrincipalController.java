package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.model.LivroFx;
import br.com.carlos.bibliotecafx.util.ConfigUtil;
import br.com.carlos.bibliotecafx.util.DialogUtil;
import br.com.carlos.bibliotecafx.util.HttpUtil;
import br.com.carlos.bibliotecafx.util.ThemeManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class TelaPrincipalController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ObservableList<LivroFx> todosOsLivros = FXCollections.observableArrayList();

    @FXML
    private VBox containerDetalhes;
    @FXML
    private ListView<LivroFx> listaLivros;
    @FXML
    private TextField txtBusca;
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
    private Label labelStatusLeitura;

    @FXML
    public void initialize() {
        configurarListView();
        configurarFiltroBusca();
        carregarLivros();
    }


    private void configurarListView() {
        listaLivros.setCellFactory(listView -> new ListCell<>() {
            private Parent graphic;
            private CelulaLivroController controller;

            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/view/CelulaLivro.fxml"));
                    graphic = loader.load();
                    controller = loader.getController();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void updateItem(LivroFx livro, boolean empty) {
                super.updateItem(livro, empty);

                if (empty || livro == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    controller.setLivro(livro);

                    if (graphic != null) {
                        // <<< CORREÇÃO AQUI: Fazendo o "cast" para (Region) >>>
                        // Isso nos dá acesso às propriedades de tamanho como prefWidthProperty.
                        ((Region) graphic).prefWidthProperty()
                                          .bind(listView.widthProperty()
                                                        .subtract(20));
                    }

                    setGraphic(graphic);
                }
            }
        });

        // O resto do método com os listeners continua igual
        listaLivros.getSelectionModel()
                   .selectedItemProperty()
                   .addListener(
                           (obs, antigo, novo) -> {
                               exibirDetalhesLivro(novo);
                               boolean desabilitar = (novo == null);
                               btnDeletarLivro.setDisable(desabilitar);
                               btnEditarLivro.setDisable(desabilitar);
                           }
                   );

        listaLivros.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.DELETE && listaLivros.getSelectionModel()
                                                                .getSelectedItem() != null) {
                deletarLivro();
            }
        });
    }

    private void configurarFiltroBusca() {
        FilteredList<LivroFx> livrosFiltrados = new FilteredList<>(todosOsLivros, p -> true);

        txtBusca.textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    livrosFiltrados.setPredicate(livro -> {
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }
                        String termoBusca = newValue.toLowerCase();
                        if (livro.getTitulo()
                                 .toLowerCase()
                                 .contains(termoBusca)) {
                            return true;
                        } else return livro.getAutor() != null && livro.getAutor()
                                                                       .toLowerCase()
                                                                       .contains(termoBusca);
                    });
                });

        listaLivros.setItems(livrosFiltrados);
        listaLivros.setPlaceholder(new Label("Carregando livros..."));
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
            todosOsLivros.setAll(carregarTask.getValue());
            if (todosOsLivros.isEmpty()) {
                listaLivros.setPlaceholder(new Label("Nenhum livro na estante."));
            }
        });

        carregarTask.setOnFailed(event -> {
            listaLivros.setPlaceholder(new Label("Falha ao carregar livros."));
            Throwable ex = carregarTask.getException();
            ex.printStackTrace();
            DialogUtil.showError("Erro de Rede", "Não foi possível buscar os livros do servidor.", ex.getMessage());
        });

        new Thread(carregarTask).start();
    }

    private void exibirDetalhesLivro(LivroFx livro) {
        boolean livroNaoSelecionado = (livro == null);

        if (livroNaoSelecionado) {
            // Limpa todos os campos se nenhum livro for selecionado
            labelTitulo.setText("Selecione um livro");
            labelAutor.setText("---");
            labelAno.setText("---");
            labelPaginas.setText("---");
            lblSinopse.setText("");
            imageCapa.setImage(null);

            // <<< ALTERAÇÃO AQUI >>>
            // Limpa o label de status também
            labelStatusLeitura.setText("---");
            labelStatusLeitura.setStyle("-fx-text-fill: black; -fx-font-weight: normal;"); // Reseta o estilo

        } else {
            // Preenche os campos com as informações do livro selecionado
            labelTitulo.setText(livro.getTitulo());
            labelAutor.setText(livro.getAutor());
            labelAno.setText(livro.getAno() == 0 ? "Não informado" : String.valueOf(livro.getAno()));
            labelPaginas.setText(livro.getNumeroPaginas() == 0 ? "Não informado" : String.valueOf(livro.getNumeroPaginas()));
            lblSinopse.setText(livro.getSinopse());

            // <<< ALTERAÇÃO AQUI >>>
            // Adiciona a lógica para definir o texto e a cor do status de leitura
            if (livro.isLido()) {
                labelStatusLeitura.setText("📗 Lido");
                labelStatusLeitura.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;"); // Verde
            } else {
                labelStatusLeitura.setText("📕 Não Lido");
                labelStatusLeitura.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // Vermelho
            }

            // Lógica para exibir a capa (permanece a mesma)
            Image imagemCapa = livro.getImagemCapa();
            if (imagemCapa != null) {
                if (imagemCapa.isError()) {
                    System.err.println("Erro ao carregar a imagem para o livro: " + livro.getTitulo());
                    imageCapa.setImage(null); // Opcional: mostrar uma imagem de placeholder
                } else {
                    imageCapa.setImage(imagemCapa);
                }
            } else {
                imageCapa.setImage(null); // Opcional: mostrar uma imagem de placeholder
            }
        }
    }

    @FXML
    private void deletarLivro() {
        LivroFx livroSelecionado = listaLivros.getSelectionModel()
                                              .getSelectedItem();

        if (livroSelecionado == null) {
            DialogUtil.showError("Nenhum Livro Selecionado",
                    "Por favor, selecione um livro na lista para excluir.", "");
            return;
        }

        Optional<ButtonType> resultado = DialogUtil.showConfirmation(
                "Confirmar Exclusão",
                "Você tem certeza que deseja excluir o livro?",
                "Livro: " + livroSelecionado.getTitulo() + "\nAutor: " + livroSelecionado.getAutor());

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
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
                listaLivros.getSelectionModel()
                           .clearSelection();
                carregarLivros(); // Recarrega a lista para refletir a exclusão
            });

            deleteTask.setOnFailed(event -> {
                Throwable ex = deleteTask.getException();
                ex.printStackTrace();
                DialogUtil.showError("Erro ao Excluir", "Ocorreu uma falha ao tentar excluir o livro.", ex.getMessage());
            });

            new Thread(deleteTask).start();
        }
    }

    @FXML
    private void editarLivroSelecionado() {
        LivroFx livroSelecionado = listaLivros.getSelectionModel()
                                              .getSelectedItem();
        if (livroSelecionado == null) return;
        try {
            String fxmlPath = ConfigUtil.getProperty("fxml.path.edicao");
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Não foi possível encontrar o arquivo FXML. Verifique a chave 'fxml.path.edicao' em config.properties.");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            TelaEdicaoLivroController controller = loader.getController();
            controller.inicializarDados(livroSelecionado);
            controller.setOnEdicaoConcluidaCallback(this::carregarLivros);

            Stage edicaoStage = new Stage();
            Scene scene = new Scene(root, containerDetalhes.getWidth(), containerDetalhes.getHeight());

            // <<< APLICA O TEMA ATUAL NA NOVA JANELA >>>
            ThemeManager.applyThemeToScene(scene);

            edicaoStage.setTitle("Editando: " + livroSelecionado.getTitulo());
            edicaoStage.initModality(Modality.WINDOW_MODAL);
            edicaoStage.initOwner(listaLivros.getScene()
                                             .getWindow());
            edicaoStage.setScene(scene);
            edicaoStage.setX(containerDetalhes.localToScreen(containerDetalhes.getBoundsInLocal())
                                              .getMinX());
            edicaoStage.setY(containerDetalhes.localToScreen(containerDetalhes.getBoundsInLocal())
                                              .getMinY());
            edicaoStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showError("Erro ao Abrir Editor", "Não foi possível carregar a tela de edição.", e.getMessage());
        }
    }

    @FXML
    private void abrirTelaCadastroManual() {
        try {
            String fxmlPath = ConfigUtil.getProperty("fxml.path.edicao");
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            TelaEdicaoLivroController controller = loader.getController();
            controller.inicializarDados(new LivroFx());
            controller.setOnEdicaoConcluidaCallback(this::carregarLivros);

            Stage stage = new Stage();
            Scene scene = new Scene(root);

            // <<< APLICA O TEMA ATUAL NA NOVA JANELA >>>
            ThemeManager.applyThemeToScene(scene);

            stage.setTitle("Adicionar Novo Livro Manualmente");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(listaLivros.getScene()
                                       .getWindow());
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Erro de Interface", "Ocorreu um erro ao carregar a tela de cadastro manual.", e.getMessage());
        }
    }

    @FXML
    private void abrirTelaBuscaOnline() {
        try {
            String fxmlPath = ConfigUtil.getProperty("fxml.path.busca");
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            TelaBuscaLivroController buscaController = loader.getController();
            buscaController.setOnBuscaConcluidaCallback(this::carregarLivros);

            Stage stage = new Stage();
            Scene scene = new Scene(root);

            // <<< APLICA O TEMA ATUAL NA NOVA JANELA >>>
            ThemeManager.applyThemeToScene(scene);

            stage.setTitle("Buscar Livro Online");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(listaLivros.getScene()
                                       .getWindow());
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Erro de Interface", "Ocorreu um erro ao carregar a tela de busca online.", e.getMessage());
        }
    }

    @FXML
    private void abrirConfiguracoes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/view/TelaConfiguracoes.fxml"));
            Parent root = loader.load();

            Stage configStage = new Stage();
            Scene scene = new Scene(root);

            // <<< APLICA O TEMA ATUAL NA NOVA JANELA >>>
            ThemeManager.applyThemeToScene(scene);

            // Em TelaPrincipalController.java, método abrirConfiguracoes()
            TelaConfiguracoesController configController = loader.getController(); // Apenas pegue o controller

            configStage.setTitle("Configurações");
            configStage.setScene(scene);
            configStage.initModality(Modality.WINDOW_MODAL);
            configStage.initOwner(listaLivros.getScene()
                                             .getWindow());
            configStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Erro de Interface", "Não foi possível carregar a tela de configurações.", e.getMessage());
        }
    }
}