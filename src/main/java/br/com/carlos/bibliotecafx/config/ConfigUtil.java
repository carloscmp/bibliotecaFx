package br.com.carlos.bibliotecafx.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {

    private static final Properties properties = new Properties();

    static {
        String fileName = "/config.properties";

        try (InputStream input = ConfigUtil.class.getResourceAsStream(fileName)) {

            if (input == null) {
                System.err.println("ERRO CRÍTICO: Não foi possível encontrar o arquivo de configuração: " + fileName);
            } else {
                properties.load(input);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
