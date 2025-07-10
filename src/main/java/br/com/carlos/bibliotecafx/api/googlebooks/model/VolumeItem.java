package br.com.carlos.bibliotecafx.api.googlebooks.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VolumeItem {
    public VolumeInfo volumeInfo;
}