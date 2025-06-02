package br.com.carlos.bibliotecafx.controller;

public class TelaPrincipalControllerSingleton {

    private static TelaPrincipalController instance;

    public static void setInstance(TelaPrincipalController controller) {
        instance = controller;
    }

    public static TelaPrincipalController getInstance() {
        return instance;
    }
}
