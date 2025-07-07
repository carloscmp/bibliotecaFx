package br.com.carlos.bibliotecafx;

import com.sun.javafx.application.LauncherImpl;

public class Launcher {

    public static void main(String[] args) {
        LauncherImpl.launchApplication(App.class, SplashPreloader.class, args);
    }
}