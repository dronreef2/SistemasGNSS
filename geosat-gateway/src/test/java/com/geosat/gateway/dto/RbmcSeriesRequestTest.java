package com.geosat.gateway.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RbmcSeriesRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void deveValidarRequestValido() {
        RbmcSeriesRequest request = new RbmcSeriesRequest();
        request.setEstacao("ALAR");
        request.setAno(2024);
        request.setDia(100);
        request.setMax(300);

        Set<ConstraintViolation<RbmcSeriesRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void deveFalharQuandoEstacaoInvalida() {
        RbmcSeriesRequest request = new RbmcSeriesRequest();
        request.setEstacao("AL"); // Apenas 2 letras, precisa 4
        request.setAno(2024);
        request.setDia(100);

        Set<ConstraintViolation<RbmcSeriesRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("4 letras"));
    }

    @Test
    void deveFalharQuandoAnoForaDoRange() {
        RbmcSeriesRequest request = new RbmcSeriesRequest();
        request.setEstacao("ALAR");
        request.setAno(1999); // Menor que 2000
        request.setDia(100);

        Set<ConstraintViolation<RbmcSeriesRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Ano mínimo"));
    }

    @Test
    void deveFalharQuandoDiaInvalido() {
        RbmcSeriesRequest request = new RbmcSeriesRequest();
        request.setEstacao("ALAR");
        request.setAno(2024);
        request.setDia(367); // Maior que 366

        Set<ConstraintViolation<RbmcSeriesRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Dia máximo"));
    }

    @Test
    void deveFalharQuandoMaxMuitoGrande() {
        RbmcSeriesRequest request = new RbmcSeriesRequest();
        request.setEstacao("ALAR");
        request.setAno(2024);
        request.setDia(100);
        request.setMax(10001); // Maior que 10000

        Set<ConstraintViolation<RbmcSeriesRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Max máximo"));
    }
}
