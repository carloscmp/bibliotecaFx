module br.com.carlos.bibliotecafx {
    //== Módulos Externos que nossa aplicação requer ==//
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;    // Necessário para o Preloader com captura de tela
    requires java.desktop;    // Necessário para as classes AWT (Robot, Toolkit)
    requires java.prefs;      // Necessário para salvar as configurações de tema
    requires com.fasterxml.jackson.databind; // Para manipulação de JSON
    requires java.net.http;   // Para o cliente HTTP moderno

    //== Pacotes que precisam ser "abertos" para reflexão por bibliotecas externas ==//

    // Abre os controllers para que o FXML Loader possa injetar os componentes (@FXML).
    opens br.com.carlos.bibliotecafx.controller to javafx.fxml;

    // Abre os modelos para que o Jackson (JSON) e o JavaFX (Properties) possam acessar seus campos.
    opens br.com.carlos.bibliotecafx.model to com.fasterxml.jackson.databind, javafx.base;

    // Abre o DTO para o Jackson e para as Properties do JavaFX.
    opens br.com.carlos.bibliotecafx.DTO to com.fasterxml.jackson.databind, javafx.base;

    // Abre os modelos da API do Google para o Jackson poder preenchê-los.
    opens br.com.carlos.bibliotecafx.api.googlebooks.model to com.fasterxml.jackson.databind;

    // Abre o pacote principal para que o FXML possa encontrar a classe App, se necessário.
    opens br.com.carlos.bibliotecafx.main to javafx.fxml;


    //== Pacotes que nossa aplicação exporta para uso interno entre os pacotes ==//

    // Exporta o pacote principal para que o Launcher possa iniciar a aplicação.
    exports br.com.carlos.bibliotecafx.main;

    // Exporta os pacotes refatorados para que suas classes sejam visíveis umas às outras.
    exports br.com.carlos.bibliotecafx.api.googlebooks;
    exports br.com.carlos.bibliotecafx.controller;
    exports br.com.carlos.bibliotecafx.model;
    exports br.com.carlos.bibliotecafx.DTO;
    exports br.com.carlos.bibliotecafx.service;
    exports br.com.carlos.bibliotecafx.persistence;
    exports br.com.carlos.bibliotecafx.ui;
    exports br.com.carlos.bibliotecafx.http;
    exports br.com.carlos.bibliotecafx.config;
}
