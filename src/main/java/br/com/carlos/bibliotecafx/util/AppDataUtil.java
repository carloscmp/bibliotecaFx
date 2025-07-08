package br.com.carlos.bibliotecafx.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe utilitária para obter o diretório de dados da aplicação
 * de forma consistente em diferentes sistemas operacionais.
 */
public final class AppDataUtil {

    private static final String APP_NAME = "BibliotecaFx";
    private static Path appDataDirectory;

    // Construtor privado para impedir a instanciação.
    private AppDataUtil() {
    }

    /**
     * Retorna o caminho para o diretório de dados da aplicação (ex: .../AppData/Roaming/BibliotecaFx).
     * O diretório é criado se não existir.
     *
     * @return O objeto Path para o diretório de dados.
     */
    public static Path getAppDataDirectory() {
        // Usa o padrão "singleton" para determinar o caminho apenas uma vez.
        if (appDataDirectory == null) {
            String os = System.getProperty("os.name")
                              .toLowerCase();
            Path userHome = Paths.get(System.getProperty("user.home"));

            if (os.contains("win")) {
                // Windows: Usa a variável de ambiente %APPDATA%
                String appDataPath = System.getenv("APPDATA");
                if (appDataPath != null) {
                    appDataDirectory = Paths.get(appDataPath, APP_NAME);
                } else {
                    appDataDirectory = userHome.resolve(".config")
                                               .resolve(APP_NAME);
                }
            } else if (os.contains("mac")) {
                // macOS: Usa a pasta Library/Application Support
                appDataDirectory = userHome.resolve("Library/Application Support")
                                           .resolve(APP_NAME);
            } else {
                // Linux e outros: Usa uma pasta oculta .config na home do usuário
                appDataDirectory = userHome.resolve(".config")
                                           .resolve(APP_NAME);
            }
        }

        // Garante que o diretório exista. Se não existir, ele é criado.
        try {
            Files.createDirectories(appDataDirectory);
        } catch (IOException e) {
            System.err.println("Erro ao criar o diretório de dados da aplicação.");
            e.printStackTrace();
        }

        return appDataDirectory;
    }
}
