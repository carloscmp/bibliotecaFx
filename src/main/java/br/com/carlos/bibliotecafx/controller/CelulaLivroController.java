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

    // O método initialize() foi removido pois não é mais necessário.

    public void setLivro(LivroFx livro) {
        lblTitulo.setText(livro.getTitulo());
        lblAutor.setText(livro.getAutor());

        if (livro.getCapa() != null && livro.getCapa().length > 0) {
            imgCapaMiniatura.setImage(new Image(new ByteArrayInputStream(livro.getCapa())));
        } else {
            imgCapaMiniatura.setImage(null);
        }
    }
}