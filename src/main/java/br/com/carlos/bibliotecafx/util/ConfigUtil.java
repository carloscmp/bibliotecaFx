package br.com.carlos.bibliotecafx.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe utilitária para carregar e fornecer acesso a propriedades de um arquivo
 * de configuração (config.properties).
 */
public class ConfigUtil {

    private static final Properties properties = new Properties();

    // Bloco estático: é executado apenas uma vez, quando a classe é carregada pela primeira vez.
    static {
        String fileName = "/config.properties";

        // Usamos try-with-resources para garantir que o InputStream seja fechado automaticamente.
        try (InputStream input = ConfigUtil.class.getResourceAsStream(fileName)) {

            if (input == null) {
                // Imprime no console de erro se o arquivo não for encontrado.
                System.err.println("ERRO CRÍTICO: Não foi possível encontrar o arquivo de configuração: " + fileName);
                // Em um cenário de produção, seria ideal lançar uma exceção para parar a aplicação.
                // throw new RuntimeException("Arquivo de configuração não encontrado: " + fileName);
            } else {
                // Carrega as propriedades do arquivo.
                properties.load(input);
            }

        } catch (IOException ex) {
            // Em caso de erro de leitura do arquivo.
            ex.printStackTrace();
        }
    }

    /**
     * Retorna o valor de uma propriedade de configuração.
     *
     * @param key A chave da propriedade (ex: "server.url").
     * @return O valor da propriedade como uma String, ou null se não for encontrado.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Retorna o valor de uma propriedade de configuração, ou um valor padrão caso a chave não seja encontrada.
     *
     * @param key A chave da propriedade.
     * @param defaultValue O valor a ser retornado como padrão se a chave não for encontrada.
     * @return O valor da propriedade ou o valor padrão.
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}