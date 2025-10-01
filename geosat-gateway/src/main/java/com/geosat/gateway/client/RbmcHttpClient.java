package com.geosat.gateway.client;

import com.geosat.gateway.metrics.RbmcMetrics;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Cliente de baixo nível para RBMC usando HttpComponents Core 5.
 * Fornece métodos simples que retornam bytes/strings.
 * Em PRs futuros: streaming para arquivos, cache, métricas detalhadas.
 */
@Component
public class RbmcHttpClient {

    private static final Logger log = LoggerFactory.getLogger(RbmcHttpClient.class);

    private final CloseableHttpClient httpClient;
    private final String baseUrl;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final Counter requestsTotal;
    private final Counter retriesTotal;
    private final Timer latencyTimer;
    private final RbmcMetrics rbmcMetrics;

    public RbmcHttpClient(CloseableHttpClient httpClient,
                          RetryRegistry retryRegistry,
                          CircuitBreakerRegistry circuitBreakerRegistry,
                          MeterRegistry meterRegistry,
                          RbmcMetrics rbmcMetrics,
                          @Value("${rbmc.base-url:https://servicodados.ibge.gov.br/api/v1/rbmc}") String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.rbmcMetrics = rbmcMetrics;
        this.retry = retryRegistry.retry("rbmcClient");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("rbmcClient");
        this.requestsTotal = meterRegistry.counter("rbmc.requests.total");
        this.retriesTotal = meterRegistry.counter("rbmc.retries.total");
        this.latencyTimer = Timer.builder("rbmc.requests.latency_seconds")
                .description("Latência das chamadas RBMC")
                .register(meterRegistry);
        // Eventos de retry incrementam contador
        this.retry.getEventPublisher().onRetry(ev -> this.retriesTotal.increment());
        // Gauge para estado do circuit breaker
        meterRegistry.gauge("rbmc.circuitbreaker.state", this.circuitBreaker, cb -> mapState(cb.getState()));
    }

    public String obterRelatorio(String estacao) throws IOException {
        String url = baseUrl + "/relatorio/" + estacao.toLowerCase();
        Timer stationTimer = rbmcMetrics.getStationLatencyTimer(estacao);
        try {
            return stationTimer.recordCallable(() -> executeWithResilience(url));
        } catch (Exception e) {
            if (e instanceof IOException ioe) {
                throw ioe;
            }
            throw new IOException("Error recording station latency: " + e.getMessage(), e);
        }
    }

    public String obterArquivo(String relativePath) throws IOException {
        String clean = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        String url = baseUrl + "/" + clean;
        return executeWithResilience(url);
    }

    protected String executeWithResilience(String url) throws IOException {
        java.util.concurrent.Callable<String> raw = () -> rawExecute(url);
        java.util.concurrent.Callable<String> decorated = io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateCallable(circuitBreaker, raw);
        decorated = io.github.resilience4j.retry.Retry.decorateCallable(retry, decorated);
        try {
            return latencyTimer.recordCallable(decorated);
        } catch (Exception e) {
            if (e instanceof IOException ioe) {
                throw ioe;
            }
            if (e.getCause() instanceof IOException ioe) {
                throw ioe; // unwrap
            }
            throw new IOException("Falha não IO ao chamar RBMC: " + e.getMessage(), e);
        }
    }

    protected String rawExecute(String url) throws IOException {
        long start = System.nanoTime();
        HttpGet get = new HttpGet(url);
        requestsTotal.increment();
        
        // ResponseHandler approach (não-deprecado)
        HttpClientResponseHandler<String> responseHandler = response -> {
            int status = response.getCode();
            if (status != HttpStatus.SC_OK) {
                throw new IOException("HTTP status " + status + " para " + url);
            }
            try (InputStream is = response.getEntity().getContent()) {
                byte[] bytes = is.readAllBytes();
                return new String(bytes, StandardCharsets.ISO_8859_1);
            }
        };
        
        try {
            return httpClient.execute(get, responseHandler);
        } catch (IOException e) {
            long elapsedMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
            log.warn("falha_http url={} elapsedMs={} msg={}", url, elapsedMs, e.getMessage());
            throw e;
        }
    }

    // Método reservado para futura adoção de streaming/multiplex H2

    private int mapState(CircuitBreaker.State state) {
        return switch (state) {
            case CLOSED -> 0;
            case OPEN -> 1;
            case HALF_OPEN -> 2;
            case DISABLED -> 3;
            case FORCED_OPEN -> 4;
            case METRICS_ONLY -> 5;
        };
    }
}
