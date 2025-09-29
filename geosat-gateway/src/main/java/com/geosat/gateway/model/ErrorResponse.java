package com.geosat.gateway.model;

import java.time.Instant;
import java.util.List;

/**
 * Representa erro simples padronizado.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {}
