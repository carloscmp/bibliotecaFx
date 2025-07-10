package br.com.carlos.bibliotecafx.main;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class SplashPreloader extends Preloader {

    private Stage preloaderStage;
    private ProgressBar progressBar;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.preloaderStage = primaryStage;

        String fxmlPath = "/biblioteca/view/TelaSplash.fxml";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

        Parent root = loader.load();
        progressBar = (ProgressBar) root.lookup("#progressBar");

        if (progressBar == null) {
            System.err.println("Atenção: ProgressBar com fx:id='progressBar' não foi encontrado no TelaSplash.fxml");
        }

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void handleProgressNotification(ProgressNotification info) {
        if (progressBar != null) {
            progressBar.setProgress(info.getProgress());
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide();
        }
    }
}