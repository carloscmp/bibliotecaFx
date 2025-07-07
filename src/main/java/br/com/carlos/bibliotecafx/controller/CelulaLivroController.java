package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.model.LivroFx;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;

public class CelulaLivroController {

    @FXML
    private ImageView imgCapaMiniatura;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblAutor;
    @FXML
    private Label lblStatusLeitura;
    @FXML
    private Label lblStatusEmprestimo;

    public void setLivro(LivroFx livro) {
        lblTitulo.setText(livro.getTitulo());
        lblAutor.setText(livro.getAutor());

        if (livro.getCapa() != null && livro.getCapa().length > 0) {
            imgCapaMiniatura.setImage(new Image(new ByteArrayInputStream(livro.getCapa())));
        } else {
            imgCapaMiniatura.setImage(null);
        }

        if (livro.isLido()) {
            lblStatusLeitura.setText("📗");
            lblStatusLeitura.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            lblStatusLeitura.setText("📕");
            lblStatusLeitura.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        }
        if (livro.isEmprestado()) {
            lblStatusEmprestimo.setText("➡️ Emprestado");
            lblStatusEmprestimo.setStyle("-fx-text-fill: #e67e22;"); // Laranja
        } else {
            lblStatusEmprestimo.setText(""); // Fica invisível se não estiver emprestado
        }
    }
}