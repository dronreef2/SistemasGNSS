package com.geosat.gateway.filter;

import com.geosat.gateway.util.CorrelationIdUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter que gerencia Correlation IDs para rastreamento de requisições.
 * Gera um ID único se não existir no header X-Correlation-ID e o adiciona ao MDC para logging.
 */
@Component
public class CorrelationIdFilter implements Filter {
    
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Obter ou gerar correlation ID
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new correlation ID: {}", correlationId);
        } else {
            log.debug("Using existing correlation ID: {}", correlationId);
        }
        
        // Adicionar ao MDC para logging
        CorrelationIdUtil.setCorrelationId(correlationId);
        
        // Adicionar ao response header
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            // Limpar MDC após processar a requisição
            CorrelationIdUtil.clearCorrelationId();
        }
    }
}
