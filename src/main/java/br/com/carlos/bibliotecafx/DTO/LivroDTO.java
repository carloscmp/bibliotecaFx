package br.com.carlos.bibliotecafx.DTO;

import java.io.InputStream;
import java.net.URL;

public class LivroDTO {

    private Long id;
    private String titulo;
    private String autor;
    private Integer ano;
    private String sinopse;
    private Integer numeroPaginas;
    private byte[] capa;
    private byte[] contraCapa;

    private String capaUrl;

    public LivroDTO() {
    }

    public LivroDTO(br.com.carlos.bibliotecafx.model.LivroFx fx) {
        this.id = fx.getId();
        this.titulo = fx.getTitulo();
        this.autor = fx.getAutor();
        this.ano = fx.getAno();
        this.sinopse = fx.getSinopse();
        this.numeroPaginas = fx.getNumeroPaginas();
        this.capa = fx.getCapa();
        this.contraCapa = fx.getContraCapa();
    }

    // Método utilitário para baixar a imagem da URL e armazenar em byte[]
    public void prepararParaEnvio() {
        if (this.capa == null && this.capaUrl != null && !this.capaUrl.isBlank()) {
            try (InputStream in = new URL(capaUrl).openStream()) {
                this.capa = in.readAllBytes();
            } catch (Exception e) {
                System.err.println("Erro ao baixar a imagem da capa: " + e.getMessage());
            }
        }
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(Integer numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public byte[] getCapa() {
        return capa;
    }

    public void setCapa(byte[] capa) {
        this.capa = capa;
    }

    public byte[] getContraCapa() {
        return contraCapa;
    }

    public void setContraCapa(byte[] contraCapa) {
        this.contraCapa = contraCapa;
    }

    public String getCapaUrl() {
        return capaUrl;
    }

    public void setCapaUrl(String capaUrl) {
        this.capaUrl = capaUrl;
    }
}
