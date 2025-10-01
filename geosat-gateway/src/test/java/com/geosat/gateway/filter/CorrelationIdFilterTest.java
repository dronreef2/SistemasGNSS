package com.geosat.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void deveGerarCorrelationIdSeNaoExistir() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);
        
        ArgumentCaptor<String> correlationIdCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(response).setHeader(eq("X-Correlation-ID"), correlationIdCaptor.capture());
        String correlationId = correlationIdCaptor.getValue();
        assertThat(correlationId).isNotNull().isNotEmpty();
        assertThat(correlationId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        verify(chain).doFilter(request, response);
    }

    @Test
    void deveUsarCorrelationIdExistenteDoHeader() throws ServletException, IOException {
        // Arrange
        String existingId = "test-correlation-id-123";
        when(request.getHeader("X-Correlation-ID")).thenReturn(existingId);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(response).setHeader("X-Correlation-ID", existingId);
        verify(chain).doFilter(request, response);
    }

    @Test
    void deveAdicionarCorrelationIdAoMDC() throws ServletException, IOException {
        // Arrange
        String testId = "mdc-test-id";
        when(request.getHeader("X-Correlation-ID")).thenReturn(testId);

        // Capturar o valor do MDC durante a execução do filter chain
        final String[] mdcValue = new String[1];
        doAnswer(invocation -> {
            mdcValue[0] = MDC.get("correlationId");
            return null;
        }).when(chain).doFilter(request, response);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(mdcValue[0]).isEqualTo(testId);
        // MDC deve ser limpo após processamento
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void deveLimparMDCApósProcessamento() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Correlation-ID")).thenReturn("cleanup-test");

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void deveLimparMDCMesmoComExcecao() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Correlation-ID")).thenReturn("exception-test");
        doThrow(new ServletException("Test exception")).when(chain).doFilter(request, response);

        // Act & Assert
        try {
            filter.doFilter(request, response, chain);
        } catch (ServletException e) {
            // Esperado
        }
        
        // MDC deve ser limpo mesmo com exceção
        assertThat(MDC.get("correlationId")).isNull();
    }
}
