package com.geosat.gateway.util;

import org.slf4j.MDC;

/**
 * Utilitário para manipular correlation IDs no MDC (Mapped Diagnostic Context).
 * Permite acesso programático ao correlation ID da thread atual.
 */
public class CorrelationIdUtil {
    
    private static final String CORRELATION_ID_KEY = "correlationId";
    
    /**
     * Obtém o correlation ID da thread atual.
     * @return correlation ID ou null se não definido
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }
    
    /**
     * Define o correlation ID para a thread atual.
     * @param correlationId o ID a ser definido
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isEmpty()) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
    }
    
    /**
     * Remove o correlation ID da thread atual.
     */
    public static void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_KEY);
    }
}
