package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.ui.DialogUtil;
import br.com.carlos.bibliotecafx.ui.WindowUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

public class TelaLoginController {


    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnEntrar;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private void abrirTelaRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/view/TelaRegistro.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            Window owner = btnEntrar.getScene()
                                    .getWindow();

            WindowUtil.configureModalStage(owner, stage, "Registar Novo Utilizador", scene);

            TelaRegistroController controller = loader.getController();
            controller.setLoginStage((Stage) owner);

            stage.show();
            owner.hide();

        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Erro de Interface", "Não foi possível abrir a tela de registo.");
        }
    }

    private void abrirTelaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/view/TelaPrincipal.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(root, 1100, 700);

            // Usa o WindowUtil para uma configuração consistente
            WindowUtil.configureModalStage(null, stage, "Minha Biblioteca", scene);
            stage.initOwner(null); // A tela principal não tem "dono"

            stage.show();
            ((Stage) btnEntrar.getScene()
                              .getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Erro Crítico", "Não foi possível carregar a tela principal.");
        }
    }

    private void setUiLoading(boolean isLoading) {
        progressIndicator.setVisible(isLoading);
        btnEntrar.setDisable(isLoading);
        txtUsername.setDisable(isLoading);
        txtPassword.setDisable(isLoading);
    }

}
