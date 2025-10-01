package com.geosat.gateway.dto;

import jakarta.validation.constraints.*;

public record EstacaoRequest(
    @NotBlank(message = "Estação é obrigatória")
    @Pattern(regexp = "^[A-Z]{4}$", message = "Código da estação deve ter 4 letras maiúsculas")
    String estacao
) {}