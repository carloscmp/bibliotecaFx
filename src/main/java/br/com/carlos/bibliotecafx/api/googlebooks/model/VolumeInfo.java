package br.com.carlos.bibliotecafx.api.googlebooks.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VolumeInfo {
    public String title;
    public List<String> authors;
    public String description;
    public int pageCount;
    public String publishedDate;
    public ImageLinks imageLinks;
}