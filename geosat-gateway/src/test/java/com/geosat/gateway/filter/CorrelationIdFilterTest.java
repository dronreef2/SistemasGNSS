package com.geosat.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void deveGerarCorrelationIdQuandoNaoFornecido() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        // Verifica se o header foi adicionado ao response
        String correlationId = response.getHeader("X-Correlation-ID");
        assertThat(correlationId).isNotNull();
        assertThat(correlationId).isNotEmpty();
        
        // Verifica se o filter chain foi chamado
        verify(chain).doFilter(request, response);
    }

    @Test
    void deveUsarCorrelationIdFornecidoNoRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        
        String expectedId = "test-correlation-id-123";
        request.addHeader("X-Correlation-ID", expectedId);

        filter.doFilter(request, response, chain);

        // Verifica se o mesmo ID foi usado no response
        String correlationId = response.getHeader("X-Correlation-ID");
        assertThat(correlationId).isEqualTo(expectedId);
        
        verify(chain).doFilter(request, response);
    }

    @Test
    void deveAdicionarCorrelationIdAoMDC() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            // Durante a execução da chain, o MDC deve conter o correlationId
            String mdcId = MDC.get("correlationId");
            assertThat(mdcId).isNotNull();
            assertThat(mdcId).isNotEmpty();
        };

        filter.doFilter(request, response, chain);

        // Após o filter, o MDC deve estar limpo
        String mdcIdAfter = MDC.get("correlationId");
        assertThat(mdcIdAfter).isNull();
    }

    @Test
    void deveLimparMDCAposExecucao() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Adiciona algo ao MDC antes
        MDC.put("correlationId", "old-value");

        filter.doFilter(request, response, chain);

        // O MDC deve estar limpo após a execução do filtro
        String mdcId = MDC.get("correlationId");
        assertThat(mdcId).isNull();
    }
}
