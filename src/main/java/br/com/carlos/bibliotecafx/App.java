package br.com.carlos.bibliotecafx;

import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    /**
     * O método init() SÓ é chamado de forma confiável quando usamos o LauncherImpl.
     * Ele será IGNORADO no nosso lançamento rápido de desenvolvimento.
     */
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
        // O caminho que você já estava usando
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/biblioteca/view/TelaPrincipal.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 600);

        primaryStage.setTitle("Minha Biblioteca");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * <<< MÉTODO MAIN REINTRODUZIDO >>>
     * Este será nosso ponto de entrada para DESENVOLVIMENTO.
     * O método launch() padrão do JavaFX chama o start() diretamente,
     * ignorando o preloader e o init().
     */
    public static void main(String[] args) {
        launch(args);
    }
}