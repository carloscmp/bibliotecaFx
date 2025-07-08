package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.DTO.LivroDTO;
import br.com.carlos.bibliotecafx.model.LivroFx;
import br.com.carlos.bibliotecafx.util.DialogUtil;
import br.com.carlos.bibliotecafx.util.LivroAPI;
import br.com.carlos.bibliotecafx.util.ThemeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TelaBuscaLivroController implements Initializable {

    private Runnable onBuscaConcluidaCallback;
    private ObservableList<LivroFx> listaPrincipalDeLivros;

    @FXML
    private TextField txtBusca;
    @FXML
    private TableView<LivroDTO> tabelaBuscaLivro;
    @FXML
    private TableColumn<LivroDTO, String> colTitulo;
    @FXML
    private TableColumn<LivroDTO, String> colAutor;
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnAdicionar;
    @FXML
    private ImageView imageCapa;
    @FXML
    private ProgressIndicator progressIndicator;

    public void setOnBuscaConcluidaCallback(Runnable callback) {
        this.onBuscaConcluidaCallback = callback;
    }

    // Novo método para receber a lista principal de livros
    public void setListaPrincipal(ObservableList<LivroFx> todosOsLivros) {
        this.listaPrincipalDeLivros = todosOsLivros;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressIndicator.setVisible(false);
        btnAdicionar.setDisable(true); // Botão começa desabilitado
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));

        tabelaBuscaLivro.getSelectionModel()
                        .selectedItemProperty()
                        .addListener((obs, antigo, novo) -> {
                            btnAdicionar.setDisable(novo == null); // Habilita o botão apenas se um livro for selecionado
                            if (novo != null) {
                                carregarCapaAutomaticamente(novo.getCapaUrl());
                            } else {
                                imageCapa.setImage(null);
                            }
                        });
    }

    @FXML
    public void buscarLivros() {
        String titulo = txtBusca.getText();
        if (titulo == null || titulo.isBlank()) {
            DialogUtil.showWarning("Aviso", "Por favor, digite um termo para a busca.");
            return;
        }

        Task<List<LivroDTO>> buscarTask = new Task<>() {
            @Override
            protected List<LivroDTO> call() throws Exception {
                return LivroAPI.buscarLivrosPorTitulo(titulo);
            }
        };

        buscarTask.setOnRunning(e -> {
            progressIndicator.setVisible(true);
            btnBuscar.setDisable(true);
        });

        buscarTask.setOnSucceeded(e -> {
            tabelaBuscaLivro.setItems(FXCollections.observableArrayList(buscarTask.getValue()));
            progressIndicator.setVisible(false);
            btnBuscar.setDisable(false);
        });

        buscarTask.setOnFailed(e -> {
            DialogUtil.showError("Erro de API", "Não foi possível buscar os livros.", e.getSource()
                                                                                       .getException()
                                                                                       .getMessage());
            e.getSource()
             .getException()
             .printStackTrace();
            progressIndicator.setVisible(false);
            btnBuscar.setDisable(false);
        });

        new Thread(buscarTask).start();
    }

    /**
     * Lógica refatorada: Não salva mais na rede.
     * Pega os dados do livro online, baixa a capa e abre a tela de edição pré-preenchida.
     */
    @FXML
    public void adicionarLivro() {
        LivroDTO selecionado = tabelaBuscaLivro.getSelectionModel()
                                               .getSelectedItem();
        if (selecionado == null) {
            DialogUtil.showWarning("Aviso", "Por favor, selecione um livro da lista para adicionar.");
            return;
        }

        // Cria uma Task para baixar a imagem da capa em segundo plano
        Task<byte[]> downloadCapaTask = new Task<>() {
            @Override
            protected byte[] call() throws Exception {
                if (selecionado.getCapaUrl() == null || selecionado.getCapaUrl()
                                                                   .isBlank()) {
                    return null;
                }
                URL url = new URL(selecionado.getCapaUrl());
                try (InputStream in = url.openStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int n;
                    while ((n = in.read(buffer)) != -1) {
                        out.write(buffer, 0, n);
                    }
                    return out.toByteArray();
                }
            }
        };

        downloadCapaTask.setOnRunning(e -> progressIndicator.setVisible(true));
        downloadCapaTask.setOnFailed(e -> progressIndicator.setVisible(false));

        downloadCapaTask.setOnSucceeded(e -> {
            progressIndicator.setVisible(false);
            byte[] capaBytes = downloadCapaTask.getValue();

            // Cria um novo objeto LivroFx com os dados encontrados
            LivroFx novoLivro = new LivroFx();
            novoLivro.setTitulo(selecionado.getTitulo());
            novoLivro.setAutor(selecionado.getAutor());
            novoLivro.setSinopse(selecionado.getSinopse());
            novoLivro.setAno(selecionado.getAno());
            novoLivro.setNumeroPaginas(selecionado.getNumeroPaginas());
            novoLivro.setCapa(capaBytes);
            // O livro começa como não lido e não emprestado por padrão

            // Abre a tela de edição com os dados pré-preenchidos
            abrirTelaDeEdicao(novoLivro);
        });

        new Thread(downloadCapaTask).start();
    }

    private void abrirTelaDeEdicao(LivroFx livro) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/view/TelaEdicaoLivro.fxml"));
            Parent root = loader.load();

            TelaEdicaoLivroController controller = loader.getController();
            controller.inicializarDados(livro, this.listaPrincipalDeLivros);
            controller.setOnEdicaoConcluidaCallback(this.onBuscaConcluidaCallback);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            ThemeManager.applyThemeToScene(scene);

            stage.setTitle("Adicionar Livro - Revisão");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnAdicionar.getScene()
                                        .getWindow());
            stage.setScene(scene);
            stage.showAndWait();

            // Fecha a tela de busca após a edição/cadastro ser concluída
            Stage stageBusca = (Stage) btnAdicionar.getScene()
                                                   .getWindow();
            stageBusca.close();

        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Erro de Interface", "Não foi possível abrir a tela de edição.", e.getMessage());
        }
    }

    private void carregarCapaAutomaticamente(String url) {
        try {
            if (url != null && !url.isBlank()) {
                imageCapa.setImage(new Image(url, true)); // true para carregar em background
            } else {
                imageCapa.setImage(null);
            }
        } catch (Exception e) {
            imageCapa.setImage(null);
        }
    }

    // O método carregarCapaManual() foi removido pois a tela de edição já possui essa funcionalidade.
}
