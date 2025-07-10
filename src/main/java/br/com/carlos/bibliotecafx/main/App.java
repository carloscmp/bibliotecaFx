package br.com.carlos.bibliotecafx.main;

import br.com.carlos.bibliotecafx.controller.TelaPrincipalController;
import br.com.carlos.bibliotecafx.ui.ThemeManager;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    public static Image APP_ICON;

    // Guarda uma referência ao controller da tela principal para podermos chamar seu método de shutdown.
    private TelaPrincipalController telaPrincipalController;

    @Override
    public void init() throws Exception {
        System.out.println("App.init(): Iniciando carregamento de recursos...");
        for (int i = 1; i <= 10; i++) {
            double progress = i / 10.0;
            notifyPreloader(new Preloader.ProgressNotification(progress));
            Thread.sleep(300);
        }
        System.out.println("App.init(): Carregamento de recursos concluído.");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/view/TelaPrincipal.fxml"));
        Parent root = loader.load();

        // <<< ALTERAÇÃO 1: Pega a instância do controller que o FXMLLoader acabou de criar >>>
        this.telaPrincipalController = loader.getController();

        Scene scene = new Scene(root, 1000, 600);

        // Aplica o tema salvo na inicialização
        ThemeManager.applyThemeToScene(scene);

        try {
            String iconPath = "/biblioteca/images/library_icon.png";
            APP_ICON = new Image(getClass().getResourceAsStream(iconPath));

            // Usamos o campo estático para definir o ícone da janela principal
            primaryStage.getIcons()
                        .add(APP_ICON);

            System.out.println("Ícone da aplicação carregado com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone da aplicação: " + e.getMessage());
        }

        primaryStage.setTitle("Minha Biblioteca");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * <<< ALTERAÇÃO 2: Sobrescreve o método stop() >>>
     * Este método é chamado automaticamente pelo JavaFX quando a aplicação é fechada.
     * É o lugar perfeito para limpar recursos, como threads em segundo plano.
     */
    @Override
    public void stop() throws Exception {
        System.out.println("Aplicação está fechando. Desligando serviços...");
        if (telaPrincipalController != null) {
            // Chama o método de desligamento que criamos no controller
            telaPrincipalController.shutdown();
        }
        super.stop(); // Chama a implementação padrão para garantir um fechamento limpo
    }

    /**
     * Ponto de entrada para desenvolvimento rápido, que ignora o preloader.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
