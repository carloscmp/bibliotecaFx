package br.com.carlos.bibliotecafx;

import com.sun.javafx.application.LauncherImpl;

public class Launcher {

    public static void main(String[] args) {
        // Esta linha é a mágica. Ela diz ao JavaFX para:
        // 1. Iniciar uma aplicação JavaFX.
        // 2. Usar a classe 'App.class' como a aplicação principal.
        // 3. MAS, antes de chamar o start() de App, carregar e exibir a classe 'SplashPreloader.class'.
        LauncherImpl.launchApplication(App.class, SplashPreloader.class, args);
    }
}