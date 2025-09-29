package com.geosat.gateway.model;


public record SnrSerieDTO(String codigo, int ano, int dia, java.util.List<SnrSampleDTO> samples) {}
