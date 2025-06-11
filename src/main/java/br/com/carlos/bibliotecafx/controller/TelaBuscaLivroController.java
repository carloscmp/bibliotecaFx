package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.DTO.LivroDTO;
import br.com.carlos.bibliotecafx.util.ConfigUtil;
import br.com.carlos.bibliotecafx.util.HttpUtil;
import br.com.carlos.bibliotecafx.util.LivroAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TelaBuscaLivroController implements Initializable {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private Runnable onBuscaConcluidaCallback;

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
    private Button btnCarregarImagem;
    @FXML
    private ImageView imageCapa;
    @FXML
    private ProgressIndicator progressIndicator;

    public void setOnBuscaConcluidaCallback(Runnable callback) {
        this.onBuscaConcluidaCallback = callback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressIndicator.setVisible(false);
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));

        tabelaBuscaLivro.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                carregarCapaAutomaticamente(novo.getCapaUrl());
            }
        });
    }

    @FXML
    public void buscarLivros() {
        String titulo = txtBusca.getText();
        if (titulo == null || titulo.isBlank()) {
            mostrarAlerta("Aviso", "Por favor, digite um termo para a busca.", Alert.AlertType.WARNING);
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
            // Lógica de finalização
            progressIndicator.setVisible(false);
            btnBuscar.setDisable(false);
        });

        buscarTask.setOnFailed(e -> {
            mostrarAlerta("Erro de Busca", "Não foi possível realizar a busca.", Alert.AlertType.ERROR);
            e.getSource().getException().printStackTrace();
            // Lógica de finalização
            progressIndicator.setVisible(false);
            btnBuscar.setDisable(false);
        });

        new Thread(buscarTask).start();
    }

    @FXML
    public void adicionarLivro() {
        LivroDTO selecionado = tabelaBuscaLivro.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta("Aviso", "Selecione um livro da tabela primeiro.", Alert.AlertType.WARNING);
            return;
        }

        Task<Void> adicionarTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                selecionado.prepararParaEnvio();
                String json = MAPPER.writeValueAsString(selecionado);
                String url = ConfigUtil.getProperty("server.url");
                HttpUtil.post(url, json);
                return null;
            }
        };

        adicionarTask.setOnRunning(e -> {
            progressIndicator.setVisible(true);
            btnAdicionar.setDisable(true);
        });

        adicionarTask.setOnSucceeded(e -> {
            mostrarAlerta("Sucesso", "Livro adicionado com sucesso!", Alert.AlertType.INFORMATION);

            if (onBuscaConcluidaCallback != null) {
                onBuscaConcluidaCallback.run();
            }

            // Lógica de finalização
            progressIndicator.setVisible(false);
            btnAdicionar.setDisable(false);

            Stage stage = (Stage) btnAdicionar.getScene().getWindow();
            stage.close();
        });

        adicionarTask.setOnFailed(e -> {
            mostrarAlerta("Erro ao Adicionar", "Não foi possível adicionar o livro.", Alert.AlertType.ERROR);
            e.getSource().getException().printStackTrace();
            
            // Lógica de finalização
            progressIndicator.setVisible(false);
            btnAdicionar.setDisable(false);
        });

        new Thread(adicionarTask).start();
    }

    private void carregarCapaAutomaticamente(String url) {
        try {
            if (url != null && !url.isBlank()) {
                imageCapa.setImage(new Image(url, true));
            } else {
                imageCapa.setImage(null);
            }
        } catch (Exception e) {
            imageCapa.setImage(null);
        }
    }

    @FXML
    public void carregarCapaManual() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Imagem da Capa");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(imageCapa.getScene().getWindow());
        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                imageCapa.setImage(new Image(fis));
            } catch (Exception e) {
                mostrarAlerta("Erro de Imagem", "Não foi possível carregar a imagem local.", Alert.AlertType.ERROR);
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}