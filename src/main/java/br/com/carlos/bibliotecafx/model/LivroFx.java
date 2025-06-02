package br.com.carlos.bibliotecafx.model;

import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;

public class LivroFx {

    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty titulo = new SimpleStringProperty();
    private final StringProperty autor = new SimpleStringProperty();
    private final IntegerProperty ano = new SimpleIntegerProperty();
    private final StringProperty sinopse = new SimpleStringProperty();
    private final IntegerProperty numeroPaginas = new SimpleIntegerProperty();
    private final ObjectProperty<byte[]> capa = new SimpleObjectProperty<>();
    private final ObjectProperty<byte[]> contraCapa = new SimpleObjectProperty<>();

    public LivroFx() {
    }

    public LivroFx(Long id, String titulo, String autor, int ano, String sinopse, int numeroPaginas, byte[] capa, byte[] contraCapa) {
        this.id.set(id);
        this.titulo.set(titulo);
        this.autor.set(autor);
        this.ano.set(ano);
        this.sinopse.set(sinopse);
        this.numeroPaginas.set(numeroPaginas);
        this.capa.set(capa);
        this.contraCapa.set(contraCapa);
    }

    // Getters, setters e propriedades
    public Long getId() {
        return id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }

    public LongProperty idProperty() {
        return id;
    }

    public String getTitulo() {
        return titulo.get();
    }

    public void setTitulo(String titulo) {
        this.titulo.set(titulo);
    }

    public StringProperty tituloProperty() {
        return titulo;
    }

    public String getAutor() {
        return autor.get();
    }

    public void setAutor(String autor) {
        this.autor.set(autor);
    }

    public StringProperty autorProperty() {
        return autor;
    }

    public int getAno() {
        return ano.get();
    }

    public void setAno(int ano) {
        this.ano.set(ano);
    }

    public IntegerProperty anoProperty() {
        return ano;
    }

    public String getSinopse() {
        return sinopse.get();
    }

    public void setSinopse(String sinopse) {
        this.sinopse.set(sinopse);
    }

    public StringProperty sinopseProperty() {
        return sinopse;
    }

    public int getNumeroPaginas() {
        return numeroPaginas.get();
    }

    public void setNumeroPaginas(int numeroPaginas) {
        this.numeroPaginas.set(numeroPaginas);
    }

    public IntegerProperty numeroPaginasProperty() {
        return numeroPaginas;
    }

    public byte[] getCapa() {
        return capa.get();
    }

    public void setCapa(byte[] capa) {
        this.capa.set(capa);
    }

    public ObjectProperty<byte[]> capaProperty() {
        return capa;
    }

    public byte[] getContraCapa() {
        return contraCapa.get();
    }

    public void setContraCapa(byte[] contraCapa) {
        this.contraCapa.set(contraCapa);
    }

    public ObjectProperty<byte[]> contraCapaProperty() {
        return contraCapa;
    }

    // Método auxiliar para exibir imagem da capa no JavaFX
    public Image getImagemCapa() {
        byte[] dados = getCapa();
        if (dados != null && dados.length > 0) {
            return new Image(new ByteArrayInputStream(dados));
        }
        return null;
    }

    public Image getImagemContraCapa() {
        byte[] dados = getContraCapa();
        if (dados != null && dados.length > 0) {
            return new Image(new ByteArrayInputStream(dados));
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("LivroFx[titulo='%s', autor='%s', ano=%d]", getTitulo(), getAutor(), getAno());
    }
}
