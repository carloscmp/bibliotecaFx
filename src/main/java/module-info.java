module br.com.carlos.bibliotecafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.logging;


    opens br.com.carlos.bibliotecafx to javafx.fxml;
    opens br.com.carlos.bibliotecafx.controller to javafx.fxml;
    opens br.com.carlos.bibliotecafx.DTO to javafx.base, com.fasterxml.jackson.databind;
    opens br.com.carlos.bibliotecafx.model to com.fasterxml.jackson.databind;
    opens br.com.carlos.bibliotecafx.util to com.fasterxml.jackson.databind;

    exports br.com.carlos.bibliotecafx;
    exports br.com.carlos.bibliotecafx.controller;
}
