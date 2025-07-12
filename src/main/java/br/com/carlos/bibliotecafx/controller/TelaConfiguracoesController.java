package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.ui.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;


public class TelaConfiguracoesController {

    @FXML
    private ComboBox<String> comboTemas;

    @FXML
    public void initialize() {
        comboTemas.getItems()
                  .addAll("Claro", "Escuro");
        comboTemas.setValue(ThemeManager.getThemePreference());

        comboTemas.setOnAction(event -> {
            String temaSelecionado = comboTemas.getValue();
            if (temaSelecionado != null) {
                ThemeManager.setAndApplyGlobalTheme(temaSelecionado);
            }
        });
    }

}