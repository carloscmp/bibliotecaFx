package br.com.carlos.bibliotecafx.util;

import br.com.carlos.bibliotecafx.App;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Classe utilitária para criar e exibir diálogos padronizados,
 * com tema e ícone da aplicação.
 */
public final class DialogUtil {

    private DialogUtil() {
    }

    public static void showError(String title, String content) {
        showError(title, null, content);
    }

    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        configureAndShowAndWait(alert);
    }

    public static void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        configureAndShowAndWait(alert);
    }

    public static void showWarning(String title, String content) {
        showWarning(title, null, content);
    }

    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        configureAndShowAndWait(alert);
    }

    public static Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        configureDialog(alert);

        return alert.showAndWait();
    }

    /**
     * <<< NOVO MÉTODO PARA DIÁLOGOS DE ENTRADA DE TEXTO >>>
     * Cria, configura e exibe um diálogo para o usuário digitar uma informação.
     */
    public static Optional<String> showTextInput(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        configureDialog(dialog);

        return dialog.showAndWait();
    }

    private static void configureAndShowAndWait(Dialog<?> dialog) {
        configureDialog(dialog);
        dialog.showAndWait();
    }

    /**
     * Aplica todas as configurações padrão (tema e ícone) a qualquer diálogo.
     */
    private static void configureDialog(Dialog<?> dialog) {
        ThemeManager.styleDialog(dialog);

        Window window = dialog.getDialogPane()
                              .getScene()
                              .getWindow();
        if (window instanceof Stage) {
            Stage stage = (Stage) window;
            if (App.APP_ICON != null) {
                stage.getIcons()
                     .add(App.APP_ICON);
            }
        }
    }
}
