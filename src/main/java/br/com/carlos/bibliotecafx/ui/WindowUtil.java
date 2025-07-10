package br.com.carlos.bibliotecafx.ui;

import br.com.carlos.bibliotecafx.main.App;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Classe utilitária para configurar e criar novas janelas (Stages)
 * de forma padronizada.
 */
public final class WindowUtil {

    private WindowUtil() {
    }

    /**
     * Configura uma nova janela modal com os padrões da aplicação (ícone, tema, etc.).
     *
     * @param owner    A janela "pai" que será bloqueada.
     * @param newStage O novo Stage a ser configurado.
     * @param title    O título para a nova janela.
     * @param scene    A cena a ser exibida na nova janela.
     */
    public static void configureModalStage(Window owner, Stage newStage, String title, Scene scene) {
        // 1. Define o título
        newStage.setTitle(title);

        // 2. Aplica o ícone global da aplicação
        if (App.APP_ICON != null) {
            newStage.getIcons()
                    .add(App.APP_ICON);
        }

        // 3. Define a cena e aplica o tema nela
        newStage.setScene(scene);
        ThemeManager.applyThemeToScene(scene);

        // 4. Configura como uma janela modal
        newStage.initModality(Modality.WINDOW_MODAL);
        newStage.initOwner(owner);
    }
}
