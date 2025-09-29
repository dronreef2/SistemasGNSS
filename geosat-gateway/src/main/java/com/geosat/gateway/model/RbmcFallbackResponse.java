package com.geosat.gateway.model;

import java.time.Instant;
import java.util.Map;

public record RbmcFallbackResponse(
        String estacao,
        String status,
        String mensagem,
        Instant timestamp,
        Map<String,Object> dadosCacheados
) {}
