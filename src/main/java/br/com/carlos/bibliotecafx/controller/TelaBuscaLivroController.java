package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.DTO.LivroDTO;
import br.com.carlos.bibliotecafx.util.HttpUtil;
import br.com.carlos.bibliotecafx.util.LivroAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class TelaBuscaLivroController implements Initializable {

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

    private LivroDTO livroSelecionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));

        tabelaBuscaLivro.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                livroSelecionado = novo;
                carregarCapaAutomaticamente(novo.getCapaUrl());
            }
        });
    }

    @FXML
    public void buscarLivros() {
        String titulo = txtBusca.getText();
        try {
            List<LivroDTO> livros = LivroAPI.buscarLivrosPorTitulo(titulo);
            ObservableList<LivroDTO> lista = FXCollections.observableArrayList(livros);
            tabelaBuscaLivro.setItems(lista);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro ao buscar livros.");
        }
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
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(imageCapa.getScene().getWindow());
        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                imageCapa.setImage(new Image(fis));
                // Opcional: salvar caminho/local da imagem no DTO ou em variável
            } catch (Exception e) {
                mostrarAlerta("Erro ao carregar imagem.");
            }
        }
    }

    @FXML
    public void adicionarLivro() {
        LivroDTO selecionado = tabelaBuscaLivro.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta("Selecione um livro primeiro.");
            return;
        }

        try {
            // 🔽 Prepara o DTO para envio (baixa a imagem, se necessário)
            selecionado.prepararParaEnvio();

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(selecionado);

            HttpUtil.post("http://localhost:8080/livro", json);

            mostrarAlerta("Livro adicionado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro ao adicionar livro.");
        }
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
