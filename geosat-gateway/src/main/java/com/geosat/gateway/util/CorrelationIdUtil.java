package com.geosat.gateway.util;

import org.slf4j.MDC;

/**
 * Utilitário para gerenciar Correlation IDs no MDC (Mapped Diagnostic Context).
 * Permite rastreamento de requisições através de logs.
 */
public class CorrelationIdUtil {
    
    private static final String CORRELATION_ID_KEY = "correlationId";
    
    /**
     * Obtém o correlation ID atual do MDC
     * @return O correlation ID ou null se não estiver definido
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }
    
    /**
     * Define o correlation ID no MDC
     * @param correlationId O ID a ser definido
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isEmpty()) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
    }
    
    /**
     * Remove o correlation ID do MDC
     */
    public static void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_KEY);
    }
}
