package com.geosat.gateway.model;

import java.util.List;

public record PosicaoSerieDTO(String codigo, int ano, int dia, String referencia, List<PosicaoSampleDTO> samples) {}
