package com.geosat.gateway.model;

import java.time.Instant;

/**
 * DTO genérico para representar metadata de arquivos (RINEX / órbitas) da RBMC.
 */
public record RbmcArquivoDTO(
        String estacao,
        String categoria, // ex: rinex2, rinex3_1s, rinex3_15s, orbitas
        String intervalo, // ex: 15s, 1s, null
        String link,
        String descricao,
        Instant ultimaAtualizacao
) {}
