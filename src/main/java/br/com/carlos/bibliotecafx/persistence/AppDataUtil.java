package br.com.carlos.bibliotecafx.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppDataUtil {

    private static final String APP_NAME = "BibliotecaFx";
    private static Path appDataDirectory;

    private AppDataUtil() {
    }

    public static Path getAppDataDirectory() {
        // Usa o padrão "singleton" para determinar o caminho apenas uma vez.
        if (appDataDirectory == null) {
            String os = System.getProperty("os.name")
                              .toLowerCase();
            Path userHome = Paths.get(System.getProperty("user.home"));

            if (os.contains("win")) {
                String appDataPath = System.getenv("APPDATA");
                if (appDataPath != null) {
                    appDataDirectory = Paths.get(appDataPath, APP_NAME);
                } else {
                    appDataDirectory = userHome.resolve(".config")
                                               .resolve(APP_NAME);
                }
            } else if (os.contains("mac")) {
                appDataDirectory = userHome.resolve("Library/Application Support")
                                           .resolve(APP_NAME);
            } else {
                appDataDirectory = userHome.resolve(".config")
                                           .resolve(APP_NAME);
            }
        }

        try {
            Files.createDirectories(appDataDirectory);
        } catch (IOException e) {
            System.err.println("Erro ao criar o diretório de dados da aplicação.");
            e.printStackTrace();
        }

        return appDataDirectory;
    }
}
