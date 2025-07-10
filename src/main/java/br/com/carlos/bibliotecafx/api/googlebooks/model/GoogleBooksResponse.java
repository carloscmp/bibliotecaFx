package br.com.carlos.bibliotecafx.api.googlebooks.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBooksResponse {
    public List<VolumeItem> items;
}