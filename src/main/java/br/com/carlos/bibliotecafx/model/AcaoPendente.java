package br.com.carlos.bibliotecafx.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AcaoPendente {
    private String tipo;
    private Long livroId;
    private LivroFx payload;

    public AcaoPendente() {
    }

    public AcaoPendente(String tipo, Long livroId, LivroFx payload) {
        this.tipo = tipo;
        this.livroId = livroId;
        this.payload = payload;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LivroFx getPayload() {
        return payload;
    }

    public void setPayload(LivroFx payload) {
        this.payload = payload;
    }

    public Long getLivroId() {
        return livroId;
    }

    public void setLivroId(Long livroId) {
        this.livroId = livroId;
    }
}