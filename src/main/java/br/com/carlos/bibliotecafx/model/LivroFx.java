package br.com.carlos.bibliotecafx.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        this.setId(id);
        this.setTitulo(titulo);
        this.setAutor(autor);
        this.setAno(ano);
        this.setSinopse(sinopse);
        this.setNumeroPaginas(numeroPaginas);
        this.setCapa(capa);
        this.setContraCapa(contraCapa);
    }

    @JsonProperty("id")
    public Long getId() {
        return id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }

    private final BooleanProperty lido = new SimpleBooleanProperty();

    @JsonIgnore
    public LongProperty idProperty() {
        return id;
    }

    @JsonProperty("titulo")
    public String getTitulo() {
        return titulo.get();
    }

    public void setTitulo(String titulo) {
        this.titulo.set(titulo);
    }

    @JsonIgnore
    public StringProperty tituloProperty() {
        return titulo;
    }

    @JsonProperty("autor")
    public String getAutor() {
        return autor.get();
    }

    public void setAutor(String autor) {
        this.autor.set(autor);
    }

    @JsonIgnore
    public StringProperty autorProperty() {
        return autor;
    }

    @JsonProperty("ano")
    public int getAno() {
        return ano.get();
    }

    public void setAno(int ano) {
        this.ano.set(ano);
    }

    @JsonIgnore
    public IntegerProperty anoProperty() {
        return ano;
    }

    @JsonProperty("sinopse")
    public String getSinopse() {
        return sinopse.get();
    }

    public void setSinopse(String sinopse) {
        this.sinopse.set(sinopse);
    }

    @JsonIgnore
    public StringProperty sinopseProperty() {
        return sinopse;
    }

    @JsonProperty("numeroPaginas")
    public int getNumeroPaginas() {
        return numeroPaginas.get();
    }

    public void setNumeroPaginas(int numeroPaginas) {
        this.numeroPaginas.set(numeroPaginas);
    }

    @JsonIgnore
    public IntegerProperty numeroPaginasProperty() {
        return numeroPaginas;
    }

    @JsonProperty("capa")
    public byte[] getCapa() {
        return capa.get();
    }

    public void setCapa(byte[] capa) {
        this.capa.set(capa);
    }

    @JsonIgnore
    public ObjectProperty<byte[]> capaProperty() {
        return capa;
    }

    @JsonProperty("contraCapa")
    public byte[] getContraCapa() {
        return contraCapa.get();
    }

    public void setContraCapa(byte[] contraCapa) {
        this.contraCapa.set(contraCapa);
    }

    @JsonIgnore
    public ObjectProperty<byte[]> contraCapaProperty() {
        return contraCapa;
    }

    
    public boolean isLido() {
        return lido.get();
    }

    public void setLido(boolean lido) {
        this.lido.set(lido);
    }

    @JsonIgnore // Importante para não confundir o Jackson
    public BooleanProperty lidoProperty() {
        return lido;
    }

    @JsonIgnore
    public Image getImagemCapa() {
        byte[] dados = getCapa();
        if (dados != null && dados.length > 0) {
            return new Image(new ByteArrayInputStream(dados));
        }
        return null;
    }

    @JsonIgnore
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
