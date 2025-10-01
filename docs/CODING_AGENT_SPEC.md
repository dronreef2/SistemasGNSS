# 🎯 Especificação Técnica - Próximas Implementações

## 📋 Escopo para Agente de Codificação

Este documento define de forma clara e detalhada as próximas implementações a serem realizadas no projeto GeoSat Gateway.

---

## 🏆 **FASE 1: Alta Prioridade**

### 1.1 Grafana Dashboards JSON

**Objetivo**: Criar dashboards Grafana pré-configurados para monitoramento do sistema.

**Arquivos a Criar**:
1. `observability/grafana/dashboards/overview-dashboard.json`
2. `observability/grafana/dashboards/resilience-dashboard.json`
3. `observability/grafana/dashboards/cache-dashboard.json`

**Especificações Técnicas**:

#### Dashboard 1: Overview (overview-dashboard.json)
```json
Painéis Necessários:
- Request Rate (requests/sec) usando rbmc.requests.total
- Average Latency (ms) usando rbmc.requests.latency_seconds
- Error Rate (%) calculado
- Active Circuit Breaker State usando rbmc.circuitbreaker.state
- HTTP Status Codes (200, 503, etc)
- JVM Memory Usage
- JVM GC Time

Configurações:
- Time range: Last 1 hour
- Refresh: 10s
- Variables: $datasource, $interval
```

#### Dashboard 2: Resilience (resilience-dashboard.json)
```json
Painéis Necessários:
- Circuit Breaker State Timeline usando rbmc.circuitbreaker.state
- Retry Count usando rbmc.retries.total
- Failure Rate (%)
- Recovery Time (time in OPEN state)
- Request Success vs Failure
- Fallback Activation Count

Configurações:
- Time range: Last 6 hours
- Refresh: 30s
- Alerting rules para estado OPEN
```

#### Dashboard 3: Cache Performance (cache-dashboard.json)
```json
Painéis Necessários:
- Cache Hit Rate (%)
- Cache Miss Rate (%)
- Redis Response Time
- Cache Size (keys count)
- Cache Evictions
- TTL Distribution

Configurações:
- Time range: Last 1 hour
- Refresh: 10s
- Variables: $estacao
```

**Validação**:
- [ ] Dashboards importam sem erros no Grafana
- [ ] Todas as queries retornam dados
- [ ] Gráficos renderizam corretamente
- [ ] Variables funcionam
- [ ] Export/import testado

---

### 1.2 Validação @Valid nos Endpoints

**Objetivo**: Adicionar validação de entrada usando Bean Validation nos endpoints REST.

**Arquivos a Modificar/Criar**:

1. **Criar DTOs de Request**:
   - `src/main/java/com/geosat/gateway/dto/RbmcSeriesRequest.java`
   - `src/main/java/com/geosat/gateway/dto/EstacaoRequest.java`

2. **Modificar Controllers**:
   - `src/main/java/com/geosat/gateway/controller/RbmcController.java`
   - `src/main/java/com/geosat/gateway/controller/EstacaoController.java`

3. **Criar Exception Handler**:
   - Expandir `GlobalExceptionHandler.java` com `@Valid` errors

**Especificações Técnicas**:

#### DTO: RbmcSeriesRequest
```java
package com.geosat.gateway.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RbmcSeriesRequest {
    
    @NotBlank(message = "Estação é obrigatória")
    @Pattern(regexp = "^[A-Z]{4}$", message = "Código da estação deve ter 4 letras maiúsculas")
    private String estacao;
    
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
}
```

#### Controller com @Valid
```java
@GetMapping("/{estacao}/snr")
public ResponseEntity<?> getSnr(@Valid @ModelAttribute RbmcSeriesRequest request) {
    // ... implementação
}

@GetMapping("/{estacao}/posicoes")
public ResponseEntity<?> getPosicoes(@Valid @ModelAttribute RbmcSeriesRequest request) {
    // ... implementação
}
```

#### Exception Handler
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
    
    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message("Dados de entrada inválidos")
            .details(errors)
            .build();
    
    return ResponseEntity.badRequest().body(errorResponse);
}
```

**Dependências**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Testes a Criar**:
- `RbmcControllerValidationTest.java`
- Casos: estação inválida, ano fora do range, dia negativo, max muito grande

**Validação**:
- [ ] Validações funcionam para todos os endpoints
- [ ] Mensagens de erro são claras
- [ ] HTTP 400 retornado em validação falha
- [ ] Testes passam

---

### 1.3 Correlation IDs (MDC)

**Objetivo**: Implementar correlation IDs para rastreamento de requisições através do sistema.

**Arquivos a Criar/Modificar**:

1. **Criar Filtro**:
   - `src/main/java/com/geosat/gateway/filter/CorrelationIdFilter.java`

2. **Criar Utilitário**:
   - `src/main/java/com/geosat/gateway/util/CorrelationIdUtil.java`

3. **Modificar**:
   - `application.yml` (logging pattern)
   - Todos os logs importantes nos services

**Especificações Técnicas**:

#### CorrelationIdFilter
```java
package com.geosat.gateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Obter ou gerar correlation ID
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Adicionar ao MDC
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        
        // Adicionar ao response header
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}
```

#### CorrelationIdUtil
```java
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
```

#### application.yml - Logging Pattern
```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{correlationId}] %-5level %logger{36} - %msg%n"
  level:
    root: INFO
    com.geosat.gateway: DEBUG
```

**Modificar Logs**:
```java
// Exemplo em RbmcService
log.info("Obtendo relatório da estação: {}", estacao);
// Automaticamente inclui [correlationId] no log
```

**Testes a Criar**:
- `CorrelationIdFilterTest.java`
- Validar que ID é gerado se não existe
- Validar que ID é propagado no header de response
- Validar que ID aparece nos logs

**Validação**:
- [ ] Header X-Correlation-ID presente em responses
- [ ] Correlation ID aparece em todos os logs
- [ ] ID é consistente através de uma requisição
- [ ] Novo ID gerado para cada requisição
- [ ] Testes passam

---

## 🥈 **FASE 2: Média Prioridade**

### 2.1 Custom Metrics Avançadas

**Objetivo**: Adicionar métricas customizadas para cache hit rate e latência por estação.

**Arquivo a Criar**:
- `src/main/java/com/geosat/gateway/metrics/RbmcMetrics.java`

**Modificar**:
- `RedisCacheService.java`
- `RbmcHttpClient.java`

**Especificações Técnicas**:

#### RbmcMetrics
```java
package com.geosat.gateway.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RbmcMetrics {
    
    private final MeterRegistry registry;
    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final ConcurrentHashMap<String, Timer> latencyByStation;
    
    public RbmcMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.cacheHits = Counter.builder("rbmc.cache.hits")
                .description("Cache hits")
                .register(registry);
        this.cacheMisses = Counter.builder("rbmc.cache.misses")
                .description("Cache misses")
                .register(registry);
        this.latencyByStation = new ConcurrentHashMap<>();
    }
    
    public void recordCacheHit() {
        cacheHits.increment();
    }
    
    public void recordCacheMiss() {
        cacheMisses.increment();
    }
    
    public double getCacheHitRate() {
        double hits = cacheHits.count();
        double misses = cacheMisses.count();
        double total = hits + misses;
        return total > 0 ? (hits / total) * 100 : 0;
    }
    
    public Timer getStationLatencyTimer(String estacao) {
        return latencyByStation.computeIfAbsent(estacao, 
            e -> Timer.builder("rbmc.requests.latency.by_station")
                    .tag("estacao", e)
                    .description("Latency by station")
                    .register(registry)
        );
    }
}
```

**Integrar em RedisCacheService**:
```java
public Optional<Map<String,Object>> getMetadata(String estacao) {
    String key = key(estacao);
    Map<String,Object> all = hashOps.entries(key);
    
    if (all == null || all.isEmpty()) {
        metrics.recordCacheMiss();
        return Optional.empty();
    }
    
    metrics.recordCacheHit();
    return Optional.of(all);
}
```

**Validação**:
- [ ] Métricas `rbmc.cache.hits` e `rbmc.cache.misses` disponíveis
- [ ] Métrica `rbmc.requests.latency.by_station` com tag estacao
- [ ] Cache hit rate calculado corretamente
- [ ] Prometheus scrape funciona

---

### 2.2 CORS Configuration

**Objetivo**: Configurar CORS adequadamente para permitir acesso do frontend.

**Arquivo a Criar**:
- `src/main/java/com/geosat/gateway/config/CorsConfig.java`

**Especificações Técnicas**:

```java
package com.geosat.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;
    
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String[] allowedMethods;
    
    @Value("${cors.max-age:3600}")
    private Long maxAge;
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList(
            "X-Correlation-ID",
            "Retry-After",
            "Content-Type"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}
```

**application.yml**:
```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8080}
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  max-age: 3600
```

**Testes**:
- Validar OPTIONS pre-flight
- Validar headers CORS em response
- Validar origins permitidas

**Validação**:
- [ ] CORS headers presentes
- [ ] OPTIONS requests funcionam
- [ ] Configurável via environment
- [ ] Testes passam

---

### 2.3 Refatorar Testes com ResponseHandler

**Objetivo**: Atualizar todos os testes que usam mocks do HttpClient para usar ResponseHandler.

**Arquivos a Modificar**:
- `RbmcHttpClientRetryTest.java`
- `RbmcHttpClientCircuitBreakerTest.java`

**Especificações Técnicas**:

```java
// Antes (deprecated):
Mockito.when(httpClient.execute(any(HttpGet.class)))
    .thenReturn(mockResponse);

// Depois (ResponseHandler):
Mockito.when(httpClient.execute(any(HttpGet.class), any(HttpClientResponseHandler.class)))
    .thenAnswer(invocation -> {
        HttpClientResponseHandler handler = invocation.getArgument(1);
        return handler.handleResponse(mockResponse);
    });
```

**Padrão a Seguir**:
```java
@Test
void deveAplicarRetryAteLimite() throws Exception {
    // Arrange
    CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
    StatusLine statusLine = Mockito.mock(StatusLine.class);
    Mockito.when(statusLine.getStatusCode()).thenReturn(500);
    Mockito.when(mockResponse.getStatusLine()).thenReturn(statusLine);
    
    Mockito.when(httpClient.execute(
        any(HttpGet.class), 
        any(HttpClientResponseHandler.class)
    )).thenThrow(new IOException("Simulated failure"));
    
    // Act & Assert
    assertThatThrownBy(() -> client.obterRelatorio("ALAR"))
        .isInstanceOf(IOException.class);
    
    // Verify
    Mockito.verify(httpClient, times(4))
        .execute(any(HttpGet.class), any(HttpClientResponseHandler.class));
}
```

**Validação**:
- [ ] Todos os testes compilam sem warnings
- [ ] Todos os testes passam
- [ ] Nenhum método deprecado usado
- [ ] Coverage mantida ou aumentada

---

## 🥉 **FASE 3: Baixa Prioridade**

### 3.1 Frontend PWA

**Objetivo**: Transformar o frontend em Progressive Web App.

**Arquivos a Criar**:
1. `frontend/web/manifest.json`
2. `frontend/web/service-worker.js`
3. `frontend/web/js/pwa.js`

**Especificações**:
- Service Worker para cache offline
- Manifest para instalação
- Icons 192x192 e 512x512
- Cache strategy: Network-first com fallback

---

### 3.2 Persistência PostgreSQL

**Objetivo**: Adicionar banco de dados para histórico de requisições.

**Tecnologias**:
- Spring Data JPA
- PostgreSQL 15+
- Flyway para migrations

**Entidades**:
- `RequestLog` (timestamp, estacao, endpoint, latency, status)
- `StationMetadata` (codigo, nome, lat, lon, status)

---

### 3.3 Rate Limiting

**Objetivo**: Implementar rate limiting para proteger a API.

**Tecnologia**: Bucket4j + Redis

**Configuração**:
- 100 requests/minute por IP
- 1000 requests/hour por API key
- Header: `X-RateLimit-Remaining`

---

## ✅ **CRITÉRIOS DE ACEITAÇÃO GERAIS**

Para cada implementação:
- [ ] Código compila sem erros
- [ ] Todos os testes passam
- [ ] Cobertura de testes >= 80%
- [ ] Documentação atualizada
- [ ] README atualizado se necessário
- [ ] Sem warnings de deprecation
- [ ] Segue padrões do projeto
- [ ] Commit messages descritivas

---

## 📦 **ENTREGÁVEIS POR FASE**

### Fase 1 (Alta Prioridade)
- [ ] 3 arquivos JSON de dashboards Grafana
- [ ] 2 DTOs novos para validação
- [ ] 2 controllers modificados com @Valid
- [ ] 1 exception handler expandido
- [ ] 2 classes novas (Filter + Util) para correlation IDs
- [ ] application.yml atualizado
- [ ] Testes para todas as funcionalidades

### Fase 2 (Média Prioridade)
- [ ] 1 classe RbmcMetrics
- [ ] 2 services modificados com métricas
- [ ] 1 CorsConfig
- [ ] 2 testes refatorados com ResponseHandler
- [ ] Configurações atualizadas

### Fase 3 (Baixa Prioridade)
- [ ] PWA files (manifest, service-worker)
- [ ] Entities JPA
- [ ] Migrations Flyway
- [ ] Rate limiting config
- [ ] Documentação completa

---

## 🎯 **PRIORIZAÇÃO RECOMENDADA**

**Sprint 1** (1 semana):
- Grafana Dashboards
- Validação @Valid
- Correlation IDs

**Sprint 2** (1 semana):
- Custom Metrics
- CORS Config
- Refatoração de Testes

**Sprint 3** (2 semanas):
- Frontend PWA
- Persistência PostgreSQL

**Sprint 4** (1 semana):
- Rate Limiting
- Documentação final
- Testes E2E

---

**Documento preparado em**: 2025-10-01
**Versão**: 1.0
**Status**: ✅ Pronto para delegação ao agente
