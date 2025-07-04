package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.model.LivroFx;
import br.com.carlos.bibliotecafx.util.ConfigUtil;
import br.com.carlos.bibliotecafx.util.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TelaEdicaoLivroController {

    @FXML
    private CheckBox checkLido;
    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtAutor;
    @FXML
    private TextField txtAno;
    @FXML
    private TextField txtPaginas;
    @FXML
    private ImageView imgCapa;
    @FXML
    private Button btnCarregarCapa;
    @FXML
    private TextArea txtSinopse;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnCancelar;

    private LivroFx livroAtual;
    private byte[] novaCapaBytes;
    private Runnable onEdicaoConcluidaCallback;

    public void inicializarDados(LivroFx livro) {
        this.livroAtual = livro;
        this.novaCapaBytes = livro.getCapa();

        txtTitulo.setText(livro.getTitulo());
        txtAutor.setText(livro.getAutor());
        txtAno.setText(livro.getAno() == 0 ? "" : String.valueOf(livro.getAno()));
        txtPaginas.setText(livro.getNumeroPaginas() == 0 ? "" : String.valueOf(livro.getNumeroPaginas()));
        txtSinopse.setText(livro.getSinopse());
        checkLido.setSelected(livro.isLido());

        if (livro.getCapa() != null && livro.getCapa().length > 0) {
            imgCapa.setImage(new Image(new ByteArrayInputStream(livro.getCapa())));
        } else {
            imgCapa.setImage(null);
        }
    }

    public void setOnEdicaoConcluidaCallback(Runnable callback) {
        this.onEdicaoConcluidaCallback = callback;
    }

    @FXML
    private void carregarNovaCapa() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione uma Imagem de Capa");
        fileChooser.getExtensionFilters()
                   .addAll(
                           new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.png", "*.jpeg", "*.gif"),
                           new FileChooser.ExtensionFilter("Todos os Arquivos", "*.*")
                   );
        File arquivoSelecionado = fileChooser.showOpenDialog(btnCarregarCapa.getScene()
                                                                            .getWindow());
        if (arquivoSelecionado != null) {
            try {
                this.novaCapaBytes = Files.readAllBytes(arquivoSelecionado.toPath());
                imgCapa.setImage(new Image(new ByteArrayInputStream(this.novaCapaBytes)));
            } catch (IOException e) {
                mostrarAlertaErro("Erro de Arquivo", "Não foi possível ler a imagem selecionada.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void salvarAlteracoes() {
        if (!validarCampos()) {
            return;
        }
        livroAtual.setLido(checkLido.isSelected());
        livroAtual.setTitulo(txtTitulo.getText());
        livroAtual.setAutor(txtAutor.getText());
        livroAtual.setAno(txtAno.getText()
                                .trim()
                                .isEmpty() ? 0 : Integer.parseInt(txtAno.getText()
                                                                        .trim()));
        livroAtual.setNumeroPaginas(txtPaginas.getText()
                                              .trim()
                                              .isEmpty() ? 0 : Integer.parseInt(txtPaginas.getText()
                                                                                          .trim()));
        livroAtual.setSinopse(txtSinopse.getText());
        livroAtual.setCapa(this.novaCapaBytes);

        Task<Void> salvarTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(livroAtual);
                String url = ConfigUtil.getProperty("server.url") + "/" + livroAtual.getId();
                HttpUtil.put(url, json);
                return null;
            }
        };

        salvarTask.setOnSucceeded(event -> Platform.runLater(() -> {
            mostrarAlertaSucesso("Sucesso", "As alterações foram salvas com sucesso!");

            if (onEdicaoConcluidaCallback != null) {
                onEdicaoConcluidaCallback.run();
            }
            cancelarEdicao();
        }));

        salvarTask.setOnFailed(event -> {
            Throwable ex = salvarTask.getException();
            ex.printStackTrace();
            Platform.runLater(() -> mostrarAlertaErro("Erro no Servidor", "Falha ao atualizar o livro: " + ex.getMessage()));
        });

        new Thread(salvarTask).start();
    }

    private boolean validarCampos() {
        String titulo = txtTitulo.getText();
        if (titulo == null || titulo.trim()
                                    .isEmpty()) {
            mostrarAlertaErro("Erro de Validação", "O campo 'Título' é obrigatório.");
            return false;
        }
        try {
            if (!txtAno.getText()
                       .trim()
                       .isEmpty()) Integer.parseInt(txtAno.getText()
                                                          .trim());
        } catch (NumberFormatException e) {
            mostrarAlertaErro("Erro de Validação", "O campo 'Ano' deve ser um número válido.");
            return false;
        }
        try {
            if (!txtPaginas.getText()
                           .trim()
                           .isEmpty()) Integer.parseInt(txtPaginas.getText()
                                                                  .trim());
        } catch (NumberFormatException e) {
            mostrarAlertaErro("Erro de Validação", "O campo 'Páginas' deve ser um número válido.");
            return false;
        }
        return true;
    }

    @FXML
    private void cancelarEdicao() {
        Stage stage = (Stage) btnCancelar.getScene()
                                         .getWindow();
        stage.close();
    }

    private void mostrarAlertaSucesso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}