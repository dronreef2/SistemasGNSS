package com.geosat.gateway.model;

import java.time.Instant;

/**
 * DTO simplificado (placeholder) para resposta de relatório.
 * Será expandido quando integração real com RBMC for implementada.
 */
public record RbmcRelatorioDTO(
        String estacao,
        String tipo,
        String link,
        String descricao,
        Long tamanhoBytes,
        Instant ultimaAtualizacao
) {}
