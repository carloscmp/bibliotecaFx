package br.com.carlos.bibliotecafx.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.carlos.bibliotecafx.model.LivroFx;
import br.com.carlos.bibliotecafx.util.HttpUtil;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TelaPrincipalController {

    @FXML
    private TableView<LivroFx> tabelaLivros;

    @FXML
    private TableColumn<LivroFx, String> colTitulo;

    @FXML
    private TableColumn<LivroFx, String> colAutor;

    @FXML
    private VBox containerDetalhes;

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

        // Ajustar propriedades das colunas
        colTitulo.setCellValueFactory(data -> data.getValue().tituloProperty());
        colAutor.setCellValueFactory(data -> data.getValue().autorProperty());

        // Impedir redimensionamento e reordenação
        colTitulo.setResizable(false);
        colAutor.setResizable(false);
        colTitulo.setReorderable(false);
        colAutor.setReorderable(false);

        // Listener para seleção de livro
        tabelaLivros.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                exibirDetalhesLivro(novo);
            }
        });

        Platform.runLater(() -> {
            tabelaLivros.prefWidthProperty().bind(tabelaLivros.getScene().widthProperty().multiply(0.2));
            containerDetalhes.prefWidthProperty().bind(containerDetalhes.getScene().widthProperty().multiply(0.8));
        });

        carregarLivros();

    }

    private void carregarLivros() {
        try {
            String json = HttpUtil.get("http://localhost:8080/livro");
            ObjectMapper mapper = new ObjectMapper();
            List<LivroFx> livros = mapper.readValue(json, new TypeReference<List<LivroFx>>() {
            });
            tabelaLivros.setItems(FXCollections.observableArrayList(livros));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exibirDetalhesLivro(LivroFx livro) {
        labelTitulo.setText(livro.getTitulo());
        labelAutor.setText(livro.getAutor());
        labelAno.setText(String.valueOf(livro.getAno()));
        labelPaginas.setText(String.valueOf(livro.getNumeroPaginas()));
        lblSinopse.setText(livro.getSinopse());

        // Exibir a imagem da capa (se existir)
        if (livro.getCapa() != null && livro.getCapa().length > 0) {
            javafx.scene.image.Image imagem = new javafx.scene.image.Image(new java.io.ByteArrayInputStream(livro.getCapa()));
            imageCapa.setImage(imagem);
        } else {
            imageCapa.setImage(null); // Limpa a imagem se não houver capa
        }
    }

    @FXML
    public void abrirTelaBusca() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/view/TelaBuscaLivro.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Buscar Livro");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
