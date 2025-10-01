package com.geosat.gateway.dto;

import jakarta.validation.constraints.*;

public class SeriesRequest {
    
    @NotBlank(message = "Código da estação é obrigatório")
    @Pattern(regexp = "^[A-Z]{4}$", message = "Código da estação deve ter 4 letras maiúsculas")
    private String codigo;
    
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 2000, message = "Ano mínimo: 2000")
    @Max(value = 2100, message = "Ano máximo: 2100")
    private Integer ano;
    
    @NotNull(message = "Dia do ano é obrigatório")
    @Min(value = 1, message = "Dia mínimo: 1")
    @Max(value = 366, message = "Dia máximo: 366")
    private Integer dia;
    
    @Min(value = 1, message = "Max mínimo: 1")
    @Max(value = 10000, message = "Max máximo: 10000")
    private Integer max = 300;

    public SeriesRequest() {
    }

    public SeriesRequest(String codigo, Integer ano, Integer dia, Integer max) {
        this.codigo = codigo;
        this.ano = ano;
        this.dia = dia;
        this.max = max;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getDia() {
        return dia;
    }

    public void setDia(Integer dia) {
        this.dia = dia;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }
}
