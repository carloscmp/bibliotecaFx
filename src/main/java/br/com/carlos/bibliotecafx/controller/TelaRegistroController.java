package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.ui.DialogUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class TelaRegistroController {

    private Stage loginStage;
    @FXML
    private Button btnRegistrar;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtConfirmPassword;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private void voltarParaLogin() {
        if (loginStage != null) {
            loginStage.show();
        }
        // Fecha a janela de registo atual
        ((Stage) btnRegistrar.getScene()
                             .getWindow()).close();
    }

    public void registrar(ActionEvent actionEvent) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        // Validação dos campos de entrada
        if (username.isBlank() || password.isBlank()) {
            DialogUtil.showWarning("Campos Vazios", "Utilizador e senha são obrigatórios.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            DialogUtil.showWarning("Senhas Diferentes", "As senhas não coincidem.");
            return;
        }
    }

    /**
     * Controla o estado da UI durante o carregamento.
     *
     * @param isLoading true para mostrar o progresso e desabilitar campos, false para o estado normal.
     */
    private void setUiLoading(boolean isLoading) {
        progressIndicator.setVisible(isLoading);
        btnRegistrar.setDisable(isLoading);
        txtUsername.setDisable(isLoading);
        txtPassword.setDisable(isLoading);
        txtConfirmPassword.setDisable(isLoading);
    }

    /**
     * Recebe a referência da janela de login para que possamos mostrá-la novamente
     * quando o utilizador voltar ou concluir o registo.
     *
     * @param loginStage O Stage da tela de login.
     */
    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
    }
}
