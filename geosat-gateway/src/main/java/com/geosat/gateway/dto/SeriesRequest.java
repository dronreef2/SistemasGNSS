package com.geosat.gateway.dto;

import jakarta.validation.constraints.*;

public record SeriesRequest(
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 2000, message = "Ano mínimo: 2000")
    @Max(value = 2100, message = "Ano máximo: 2100")
    Integer ano,

    @NotNull(message = "Dia do ano é obrigatório")
    @Min(value = 1, message = "Dia mínimo: 1")
    @Max(value = 366, message = "Dia máximo: 366")
    Integer dia,

    @Min(value = 1, message = "Max mínimo: 1")
    @Max(value = 10000, message = "Max máximo: 10000")
    Integer max
) {
    public SeriesRequest {
        if (max == null) max = 300;
    }
}