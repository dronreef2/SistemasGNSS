package com.geosat.gateway.filter;

import com.geosat.gateway.util.CorrelationIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        chain = Mockito.mock(FilterChain.class);
    }

    @Test
    void deveGerarNovoCorrelationIdSeNaoExistir() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(response).setHeader(eq("X-Correlation-ID"), anyString());
        verify(chain).doFilter(request, response);
        
        // Verificar que o MDC foi limpo após a execução
        assertThat(CorrelationIdUtil.getCorrelationId()).isNull();
    }

    @Test
    void deveUsarCorrelationIdExistente() throws ServletException, IOException {
        // Arrange
        String existingId = "test-correlation-id-123";
        when(request.getHeader("X-Correlation-ID")).thenReturn(existingId);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(response).setHeader("X-Correlation-ID", existingId);
        verify(chain).doFilter(request, response);
        
        // Verificar que o MDC foi limpo após a execução
        assertThat(CorrelationIdUtil.getCorrelationId()).isNull();
    }

    @Test
    void deveGerarNovoIdSeHeaderVazio() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Correlation-ID")).thenReturn("");

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(response).setHeader(eq("X-Correlation-ID"), anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void deveLimparMdcMesmoEmCasoDeExcecao() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);
        doThrow(new ServletException("Test exception")).when(chain).doFilter(request, response);

        // Act & Assert
        try {
            filter.doFilter(request, response, chain);
        } catch (ServletException e) {
            // Esperado
        }

        // Verificar que o MDC foi limpo mesmo com exceção
        assertThat(CorrelationIdUtil.getCorrelationId()).isNull();
    }
}
