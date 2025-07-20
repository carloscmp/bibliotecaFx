package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.config.ConfigUtil;
import br.com.carlos.bibliotecafx.http.HttpUtil;
import br.com.carlos.bibliotecafx.model.AcaoPendente;
import br.com.carlos.bibliotecafx.model.LivroFx;
import br.com.carlos.bibliotecafx.persistence.AppDataUtil;
import br.com.carlos.bibliotecafx.persistence.FilaSincronizacao;
import br.com.carlos.bibliotecafx.persistence.GerenciadorDadosLocal;
import br.com.carlos.bibliotecafx.service.ServicoSincronizacao;
import br.com.carlos.bibliotecafx.ui.DialogUtil;
import br.com.carlos.bibliotecafx.ui.WindowUtil;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TelaPrincipalController {

    private final ObservableList<LivroFx> todosOsLivros = FXCollections.observableArrayList();
    private ScheduledExecutorService executorSincronizacao;

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
    private Hyperlink linkEmprestimo;

    @FXML
    public void initialize() {
        configurarListView();
        configurarFiltroBusca();
        sincronizacaoInicialSeNecessario();
        iniciarServicoDeSincronizacao();
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
                        ((Region) graphic).prefWidthProperty()
                                          .bind(listView.widthProperty()
                                                        .subtract(20));
                    }
                    setGraphic(graphic);
                }
            }
        });

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

    private void sincronizacaoInicialSeNecessario() {
        if (GerenciadorDadosLocal.carregarBiblioteca()
                                 .isEmpty()) {
            System.out.println("Arquivo local não encontrado. Realizando sincronização inicial com o servidor...");
            Task<List<LivroFx>> task = new Task<>() {
                @Override
                protected List<LivroFx> call() throws Exception {
                    String url = ConfigUtil.getProperty("server.url");
                    return new ObjectMapper().readValue(HttpUtil.get(url), new TypeReference<>() {
                    });
                }
            };
            task.setOnSucceeded(e -> {
                List<LivroFx> livrosDoServidor = task.getValue();
                GerenciadorDadosLocal.salvarBiblioteca(livrosDoServidor);
                todosOsLivros.setAll(livrosDoServidor);
                System.out.println("Sincronização inicial concluída. " + livrosDoServidor.size() + " livros carregados.");
            });
            task.setOnFailed(e -> {
                e.getSource()
                 .getException()
                 .printStackTrace();
                DialogUtil.showError("Erro de Rede", "Não foi possível realizar a sincronização inicial com o servidor.");
                carregarLivrosLocalmente();
            });
            new Thread(task).start();
        } else {
            carregarLivrosLocalmente();
        }
    }

    private void carregarLivrosLocalmente() {
        System.out.println("Carregando livros do arquivo local...");
        todosOsLivros.setAll(GerenciadorDadosLocal.carregarBiblioteca());
        if (todosOsLivros.isEmpty()) {
            listaLivros.setPlaceholder(new Label("Nenhum livro na estante. Adicione um!"));
        }
        listaLivros.getSelectionModel()
                   .clearSelection();
    }

    @FXML
    private void abrirLocalSave() {
        // Verifica se a funcionalidade de Desktop é suportada pelo sistema atual.
        if (Desktop.isDesktopSupported()) {
            try {
                // 1. Obtém o caminho para a pasta de dados usando a nossa AppDataUtil.
                File saveDirectory = AppDataUtil.getAppDataDirectory()
                                                .toFile();

                // 2. Garante que o diretório realmente existe antes de tentar abri-lo.
                if (saveDirectory.exists()) {
                    System.out.println("Abrindo diretório: " + saveDirectory.getAbsolutePath());
                    // 3. Dá a ordem para o sistema operativo abrir a pasta.
                    Desktop.getDesktop()
                           .open(saveDirectory);
                } else {
                    System.err.println("O diretório de salvamento não existe: " + saveDirectory.getAbsolutePath());
                    DialogUtil.showError("Erro", "O diretório de salvamento ainda não foi criado.");
                }
            } catch (IOException e) {
                System.err.println("Falha ao tentar abrir o diretório de salvamento.");
                e.printStackTrace();
                DialogUtil.showError("Erro", "Não foi possível abrir a pasta de salvamento.", e.getMessage());
            }
        } else {
            System.err.println("A funcionalidade Desktop não é suportada neste sistema.");
            DialogUtil.showWarning("Funcionalidade Indisponível", "Não é possível abrir pastas automaticamente neste sistema operativo.");
        }
    }

    private void iniciarServicoDeSincronizacao() {
        executorSincronizacao = Executors.newSingleThreadScheduledExecutor();
        executorSincronizacao.scheduleAtFixedRate(new ServicoSincronizacao(), 10, 300, TimeUnit.SECONDS);
    }

    public void shutdown() {
        if (executorSincronizacao != null && !executorSincronizacao.isShutdown()) {
            System.out.println("Desligando serviço de sincronização...");
            executorSincronizacao.shutdownNow();
        }
    }

    private void exibirDetalhesLivro(LivroFx livro) {
        boolean livroNaoSelecionado = (livro == null);
        if (livroNaoSelecionado) {
            labelTitulo.setText("Selecione um livro");
            labelAutor.setText("---");
            labelAno.setText("---");
            labelPaginas.setText("---");
            lblSinopse.setText("");
            imageCapa.setImage(null);
            labelStatusLeitura.setText("---");
            labelStatusLeitura.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
            linkEmprestimo.setText("---");
            linkEmprestimo.setOnAction(null);
        } else {
            labelTitulo.setText(livro.getTitulo());
            labelAutor.setText(livro.getAutor());
            labelAno.setText(livro.getAno() == 0 ? "Não informado" : String.valueOf(livro.getAno()));
            labelPaginas.setText(livro.getNumeroPaginas() == 0 ? "Não informado" : String.valueOf(livro.getNumeroPaginas()));
            lblSinopse.setText(livro.getSinopse());

            if (livro.isLido()) {
                labelStatusLeitura.setText("📗 Lido");
                labelStatusLeitura.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            } else {
                labelStatusLeitura.setText("📕 Não Lido");
                labelStatusLeitura.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            }
            if (livro.isEmprestado()) {
                linkEmprestimo.setText("➡️ Emprestado para: " + livro.getEmprestadoPara());
                linkEmprestimo.setStyle("-fx-text-fill: #e67e22;");
                linkEmprestimo.setOnAction(event -> marcarComoDevolvido(livro));
            } else {
                linkEmprestimo.setText("✔️ Disponível (clique para emprestar)");
                linkEmprestimo.setStyle("-fx-text-fill: #28a745;");
                linkEmprestimo.setOnAction(event -> registrarNovoEmprestimo(livro));
            }
            Image imagemCapa = livro.getImagemCapa();
            if (imagemCapa != null) {
                if (imagemCapa.isError()) {
                    System.err.println("Erro ao carregar a imagem para o livro: " + livro.getTitulo());
                    imageCapa.setImage(null);
                } else {
                    imageCapa.setImage(imagemCapa);
                }
            } else {
                imageCapa.setImage(null);
            }
        }
    }

    @FXML
    private void deletarLivro() {
        LivroFx livroSelecionado = listaLivros.getSelectionModel()
                                              .getSelectedItem();
        if (livroSelecionado == null) {
            DialogUtil.showWarning("Nenhum Livro Selecionado", "Por favor, selecione um livro para excluir.");
            return;
        }

        Optional<ButtonType> resultado = DialogUtil.showConfirmation(
                "Confirmar Exclusão",
                "Você tem certeza que deseja excluir o livro?",
                "Livro: " + livroSelecionado.getTitulo()
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            todosOsLivros.remove(livroSelecionado);
            GerenciadorDadosLocal.salvarBiblioteca(todosOsLivros);
            System.out.println("Livro removido localmente: " + livroSelecionado.getTitulo());

            if (livroSelecionado.getId() > 0) {
                FilaSincronizacao.adicionarOuAtualizarAcao(new AcaoPendente("DELETE", livroSelecionado.getId(), null));
                System.out.println("Ação DELETE adicionada à fila para o livro ID: " + livroSelecionado.getId());
            }
        }
    }

    private void registrarNovoEmprestimo(LivroFx livro) {
        // Uma única chamada ao nosso novo método utilitário
        Optional<String> resultado = DialogUtil.showTextInput(
                "Registrar Empréstimo",
                "Para quem você está emprestando '" + livro.getTitulo() + "'?",
                "Nome:"
        );

        // A lógica para processar o resultado continua a mesma
        resultado.ifPresent(nome -> {
            if (!nome.trim()
                     .isEmpty()) {
                livro.setEmprestado(true);
                livro.setEmprestadoPara(nome);
                GerenciadorDadosLocal.salvarBiblioteca(todosOsLivros);
                FilaSincronizacao.adicionarOuAtualizarAcao(new AcaoPendente("UPDATE", livro.getId(), livro));
                exibirDetalhesLivro(livro);
                listaLivros.refresh();
            }
        });
    }

    private void marcarComoDevolvido(LivroFx livro) {
        Optional<ButtonType> resultado = DialogUtil.showConfirmation(
                "Confirmar Devolução", "Deseja marcar este livro como devolvido?", "Livro: " + livro.getTitulo());

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            livro.setEmprestado(false);
            livro.setEmprestadoPara(null);
            GerenciadorDadosLocal.salvarBiblioteca(todosOsLivros);
            FilaSincronizacao.adicionarOuAtualizarAcao(new AcaoPendente("UPDATE", livro.getId(), livro));
            exibirDetalhesLivro(livro);
            listaLivros.refresh();
        }
    }

    @FXML
    private void editarLivroSelecionado() {
        LivroFx livroSelecionado = listaLivros.getSelectionModel()
                                              .getSelectedItem();
        if (livroSelecionado == null) return;
        abrirTelaDeEdicao(livroSelecionado);
    }

    @FXML
    private void abrirTelaCadastroManual() {
        abrirTelaDeEdicao(new LivroFx());
    }

    private void abrirTelaDeEdicao(LivroFx livro) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ConfigUtil.getProperty("fxml.path.edicao")));
            Parent root = loader.load();

            TelaEdicaoLivroController controller = loader.getController();
            controller.inicializarDados(livro, todosOsLivros);
            controller.setOnEdicaoConcluidaCallback(this::carregarLivrosLocalmente);

            // Declaração e uso consistente da variável 'stage'
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            Window owner = listaLivros.getScene()
                                      .getWindow();
            String title = (livro.getId() == null || livro.getId() == 0) ? "Adicionar Novo Livro" : "Editando: " + livro.getTitulo();

            WindowUtil.configureModalStage(owner, stage, title, scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Erro de Interface", "Não foi possível carregar a tela de edição.", e.getMessage());
        }
    }

    @FXML
    private void abrirTelaBuscaOnline() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ConfigUtil.getProperty("fxml.path.busca")));
            Parent root = loader.load();

            TelaBuscaLivroController buscaController = loader.getController();
            buscaController.setOnBuscaConcluidaCallback(this::carregarLivrosLocalmente);
            buscaController.setListaPrincipal(this.todosOsLivros);

            // Declaração e uso consistente da variável 'stage'
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            Window owner = listaLivros.getScene()
                                      .getWindow();

            WindowUtil.configureModalStage(owner, stage, "Buscar Livro Online", scene);
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

            // Declaração e uso consistente da variável 'stage'
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            Window owner = listaLivros.getScene()
                                      .getWindow();

            WindowUtil.configureModalStage(owner, stage, "Configurações", scene);

            // A linha abaixo não é mais necessária pois o WindowUtil não precisa da referência do controller
            // TelaConfiguracoesController configController = loader.getController();

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Erro de Interface", "Não foi possível carregar a tela de configurações.", e.getMessage());
        }
    }
}
