package br.com.carlos.bibliotecafx;

import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

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

        // <<< ADICIONANDO O ÍCONE DA APLICAÇÃO >>>
        try {
            // 1. Cria um objeto Image carregando o seu arquivo da pasta de recursos.
            //    O caminho absoluto começa com "/" a partir da raiz de 'resources'.
            String iconPath = "/biblioteca/images/library_icon.png";
            Image appIcon = new Image(getClass().getResourceAsStream(iconPath));

            // 2. Adiciona o ícone à lista de ícones da janela principal.
            primaryStage.getIcons()
                        .add(appIcon);

            System.out.println("Ícone da aplicação carregado com sucesso de: " + iconPath);

        } catch (Exception e) {
            // Se o ícone não for encontrado, a aplicação não vai quebrar,
            // mas um erro será impresso no console.
            System.err.println("Erro ao carregar o ícone da aplicação: " + e.getMessage());
            e.printStackTrace();
        }
        // <<< FIM DA SEÇÃO DO ÍCONE >>>

        primaryStage.setTitle("Minha Biblioteca");
        primaryStage.setScene(scene);
        primaryStage.show();

//        Image icon = new Image(getClass().getResourceAsStream("images/library_icon.png"));
//        primaryStage.getIcons()
//                    .add(icon);
    }


    public static void main(String[] args) {
        launch(args);
    }
}