package br.com.carlos.bibliotecafx.util;

import br.com.carlos.bibliotecafx.App;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String BASE_CSS_PATH = "/biblioteca/css/base.css";
    private static final String DARK_THEME_PATH = "/biblioteca/css/dark-theme.css";
    private static final String PREF_KEY = "theme";

    public static void applyThemeToScene(Scene scene) {
        String baseCss = App.class.getResource(BASE_CSS_PATH)
                                  .toExternalForm();
        if (!scene.getStylesheets()
                  .contains(baseCss)) {
            scene.getStylesheets()
                 .add(baseCss);
        }
        String theme = getThemePreference();
        String darkThemeCss = App.class.getResource(DARK_THEME_PATH)
                                       .toExternalForm();
        scene.getStylesheets()
             .remove(darkThemeCss);
        if ("Escuro".equals(theme)) {
            scene.getStylesheets()
                 .add(darkThemeCss);
        }
    }

    /**
     * <<< NOVO MÉTODO PARA ESTILIZAR DIÁLOGOS E ALERTAS >>>
     * Aplica os arquivos CSS corretos diretamente no painel de um diálogo.
     *
     * @param dialog O diálogo (ou alerta) a ser estilizado.
     */
    public static void styleDialog(Dialog<?> dialog) {
        // Pega o painel de conteúdo do diálogo
        var pane = dialog.getDialogPane();
        // Limpa quaisquer estilos antigos para garantir
        pane.getStylesheets()
            .clear();
        // Adiciona nosso CSS base e, condicionalmente, o tema escuro
        pane.getStylesheets()
            .add(App.class.getResource(BASE_CSS_PATH)
                          .toExternalForm());

        String theme = getThemePreference();
        if ("Escuro".equals(theme)) {
            pane.getStylesheets()
                .add(App.class.getResource(DARK_THEME_PATH)
                              .toExternalForm());
        }
    }

    public static void setAndApplyGlobalTheme(String themeName) {
        saveThemePreference(themeName);
        for (Window window : Window.getWindows()) {
            if (window.isShowing()) {
                applyThemeToScene(window.getScene());
            }
        }
    }

    private static void saveThemePreference(String themeName) {
        Preferences prefs = Preferences.userNodeForPackage(App.class);
        prefs.put(PREF_KEY, themeName);
    }

    public static String getThemePreference() {
        Preferences prefs = Preferences.userNodeForPackage(App.class);
        return prefs.get(PREF_KEY, "Claro");
    }
}