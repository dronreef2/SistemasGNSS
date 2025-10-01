package com.geosat.gateway.util;

import org.slf4j.MDC;

public class CorrelationIdUtil {

    private static final String CORRELATION_ID_KEY = "correlationId";

    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
    }

    public static void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_KEY);
    }
}