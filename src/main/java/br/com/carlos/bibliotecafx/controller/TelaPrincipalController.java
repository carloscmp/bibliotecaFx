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

public class TelaPrincipalController {

    // Instância única e reutilizável do ObjectMapper
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
    public void initialize() {
        configurarTabela();
        carregarLivros();
    }

    private void configurarTabela() {
        colTitulo.setCellValueFactory(data -> data.getValue().tituloProperty());
        colAutor.setCellValueFactory(data -> data.getValue().autorProperty());

        tabelaLivros.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> exibirDetalhesLivro(novo)
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
        if (livro == null) {
            labelTitulo.setText("");
            labelAutor.setText("");
            labelAno.setText("");
            labelPaginas.setText("");
            lblSinopse.setText("");
            imageCapa.setImage(null);
            return;
        }

        labelTitulo.setText(livro.getTitulo());
        labelAutor.setText(livro.getAutor());
        labelAno.setText(String.valueOf(livro.getAno()));
        labelPaginas.setText(String.valueOf(livro.getNumeroPaginas()));
        lblSinopse.setText(livro.getSinopse());

        if (livro.getCapa() != null && livro.getCapa().length > 0) {
            Image imagem = new Image(new java.io.ByteArrayInputStream(livro.getCapa()));
            imageCapa.setImage(imagem);
        } else {
            imageCapa.setImage(null);
        }
    }

    @FXML
    public void abrirTelaBusca() {
        try {
            String fxmlPath = ConfigUtil.getProperty("fxml.path.busca");

            if (fxmlPath == null || fxmlPath.isBlank()) {
                mostrarAlertaErro("Erro de Configuração", "O caminho para a tela de busca não foi encontrado.", "Verifique a chave 'fxml.path.busca' no arquivo config.properties.");
                return;
            }

            URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                mostrarAlertaErro("Erro de Carregamento de FXML", "Não foi possível encontrar o arquivo FXML no caminho especificado.", "Caminho verificado: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Pega o controller da tela de busca para configurar o callback
            TelaBuscaLivroController buscaController = loader.getController();
            buscaController.setOnBuscaConcluidaCallback(this::carregarLivros);

            Stage stage = new Stage();
            stage.setTitle("Buscar Livro");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
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
