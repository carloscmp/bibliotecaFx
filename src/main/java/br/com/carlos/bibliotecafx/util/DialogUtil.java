package br.com.carlos.bibliotecafx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.Optional;

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
        styleAndShowAndWait(alert);
    }

    public static void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAndShowAndWait(alert);
    }

    public static void showWarning(String title, String content) {
        showWarning(title, null, content);
    }

    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleAndShowAndWait(alert);
    }

    public static Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ThemeManager.styleDialog(alert); // Estiliza
        return alert.showAndWait(); // Espera e retorna a resposta
    }

    private static void styleAndShowAndWait(Dialog<?> dialog) {
        ThemeManager.styleDialog(dialog);
        dialog.showAndWait();
    }
}