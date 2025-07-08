package br.com.carlos.bibliotecafx.util;

import br.com.carlos.bibliotecafx.App;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String THEME_CSS_PATH = "/biblioteca/css/theme.css";
    private static final String DARK_STYLE_CLASS = "dark";
    private static final String PREF_KEY = "theme";

    /**
     * Aplica o tema correto em uma cena, adicionando a folha de estilo base
     * e a classe do tema escuro, se necessário.
     */
    public static void applyThemeToScene(Scene scene) {
        String themeCss = App.class.getResource(THEME_CSS_PATH)
                                   .toExternalForm();
        if (!scene.getStylesheets()
                  .contains(themeCss)) {
            scene.getStylesheets()
                 .add(themeCss);
        }
        updateThemeClass(scene);
    }

    /**
     * Aplica o tema em um diálogo.
     */
    public static void styleDialog(Dialog<?> dialog) {
        var pane = dialog.getDialogPane();
        pane.getStylesheets()
            .clear();
        String themeCss = App.class.getResource(THEME_CSS_PATH)
                                   .toExternalForm();
        pane.getStylesheets()
            .add(themeCss);

        if ("Escuro".equals(getThemePreference())) {
            pane.getStyleClass()
                .add(DARK_STYLE_CLASS);
        }
    }

    /**
     * Define um novo tema, salva a preferência e atualiza todas as janelas abertas.
     */
    public static void setAndApplyGlobalTheme(String themeName) {
        saveThemePreference(themeName);
        for (Window window : Window.getWindows()) {
            if (window.isShowing()) {
                updateThemeClass(window.getScene());
            }
        }
    }

    /**
     * Adiciona ou remove a classe .dark do nó raiz da cena,
     * com base na preferência salva.
     */
    private static void updateThemeClass(Scene scene) {
        String theme = getThemePreference();
        if ("Escuro".equals(theme)) {
            if (!scene.getRoot()
                      .getStyleClass()
                      .contains(DARK_STYLE_CLASS)) {
                scene.getRoot()
                     .getStyleClass()
                     .add(DARK_STYLE_CLASS);
            }
        } else {
            scene.getRoot()
                 .getStyleClass()
                 .remove(DARK_STYLE_CLASS);
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
