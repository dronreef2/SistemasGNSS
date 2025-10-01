# Conjunto de Instruções e Ferramentas (Toolkit)

Este documento consolida instruções práticas e ferramentas automatizáveis para construir o projeto GeoSat Gateway conforme o README principal.

## 1. Fluxo Macro de Entrega (PRs Sequenciais)
1. Arquitetura & Scaffold
2. Http Client + Resilience4j (retry/circuit breaker)
3. Cache Redis + Fallback
4. Observabilidade (Metrics/Prometheus/Grafana)
5. Frontend React
6. Containerização, K8s Manifests, CI/CD

Cada PR deve conter: descrição clara, testes (mínimo), instruções de execução local e checklist de critérios de aceitação.

## 2. Estrutura de Pastas Recomendada
```
geosat-gateway/
  src/main/java/com/geosat/gateway/... (controllers, services, client, config, model)
  src/main/resources/ (application.yml, logback.xml, openapi.yaml)
frontend/react-app/
infra/k8s/ (deployment.yaml, service.yaml, configmap.yaml, secret.yaml)
plantuml/ (architecture.puml)
docker/ (prometheus/, grafana/)
```

## 3. Ferramentas Técnicas
- Build: Maven (Java 17 + Spring Boot 3.x)
- HTTP: Apache HttpComponents Core 5.x
- Resiliência: Resilience4j (retry, circuit breaker, bulkhead opcional)
- Cache: Redis (Lettuce driver via Spring Data Redis)
- Testes: JUnit 5, Mockito, Spring Boot Test, Testcontainers (Redis)
- Observabilidade: Micrometer, Actuator, Prometheus, Grafana
- Qualidade: Checkstyle ou Spotless + Jacoco cobertura >= 75%
- Segurança: Rate limiting simples (bucket in-memory) + validação de parâmetros
- Logs: Logback JSON (incluir traceId/spanId se usar Spring Cloud Sleuth ou Micrometer Tracing)

## 4. Sequência Detalhada de Implementação
### 4.1 PR #1 Scaffold
- Criar projeto Spring Boot (pom.xml) com dependências básicas.
- Endpoints stub retornando 501/placeholder.
- Adicionar `architecture.puml` e gerar PNG (script opcional).
- README parcial com instruções de run local.

### 4.2 PR #2 Http Client + Resilience
- Implementar `RbmcHttpClient` com métodos: getRelatorio(estacao), getRinex2(estacao, ano, dia), getRinex3(...), getOrbitas(ano, dia).
- Timeout connect/read configurável via properties.
- Retry + circuit breaker nomeados `rbmcClient`.
- Métricas de latência via Timer.
- Testes unitários: simular 2 falhas + sucesso (retry) e abrir circuit breaker.

### 4.3 PR #3 Cache Redis + Fallback
- Armazenar metadados (JSON) com TTL 12h.
- Service: ao falhar chamada e circuit breaker aberto => devolver cache se existir; caso contrário 503 cômico.
- Testcontainers para validar fallback real.

### 4.4 PR #4 Observabilidade
- Expor /actuator/prometheus.
- Prometheus scrape config + docker-compose.
- Dashboard Grafana JSON (painéis: latência, taxa erro, estado CB, retries).

### 4.5 PR #5 Frontend
- React app (Vite ou CRA) consumindo endpoints.
- Página principal: lista de estações consultadas recentemente (cache) e status.

### 4.6 PR #6 Infra & CI
- Dockerfile multi-stage (builder + runtime distroless/alpine).
- docker-compose: app + redis + prometheus + grafana.
- K8s: Deployment, Service, ConfigMap (RBMC_BASE_URL, timeouts), Secret placeholders.
- GitHub Actions workflow (build → test → lint → package → (optional push)).

## 5. Padrões de Código
- DTOs imutáveis (Java records ou Lombok @Value se permitido).
- Controller fino → delega para Service → delega para Client.
- Exceções custom: RbmcClientException, RbmcUnavailableException.
- Resilience4j annotations (@Retry, @CircuitBreaker) ou programático.

## 6. Configurações (application.yml exemplo)
```yaml
server:
  port: 8080
rbmc:
  base-url: https://servicodados.ibge.gov.br/api/v1/rbmc
  timeouts:
    connect-ms: 3000
    response-ms: 10000
spring:
  data:
    redis:
      host: localhost
      port: 6379
resilience4j:
  retry:
    instances:
      rbmcClient:
        max-attempts: 4
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
  circuitbreaker:
    instances:
      rbmcClient:
        slidingWindowSize: 20
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    tags:
      application: geosat-gateway
```

## 7. Métricas Chave (nomes)
- rbmc.requests.total (Counter)
- rbmc.requests.latency_seconds (Histogram/Timer)
- rbmc.retries.total (Counter via Retry events)
- rbmc.circuitbreaker.state (Gauge / mapping estado)

## 8. Logging Estruturado
Campos mínimos: timestamp, level, service, traceId, spanId, endpoint, estacao, durationMs, statusCode, message.
Configurar encoder JSON no Logback e MDC para traceId/estacao.

## 9. Rate Limiting Simples
Implementar filtro Web (OncePerRequestFilter) com token bucket in-memory (ConcurrentHashMap<chaveIP, bucket>). Chaves expiram após inatividade (Scheduled cleanup). Limite sugerido: 30 req / 30s por IP.

## 10. Test Strategy
- Unit: lógica pura (Service, Client adaptador com mocks).
- Integration: chamar endpoints Spring com MockMvc + WireMock para RBMC.
- Testcontainers: subir Redis para validar fallback real.
- E2E script: docker compose up – verificar /health e uma rota rbmc.

## 11. Scripts Recomendados (a criar futuramente)
- scripts/gen-plantuml-png.sh -> converte puml em png (usa plantuml.jar ou docker). 
- scripts/run-e2e.sh -> sobe docker compose, roda checagens curl e derruba.
- scripts/dev-build.sh -> mvn -q clean verify.

## 12. OpenAPI
Gerar `openapi.yaml` manual ou via springdoc-openapi; publicar em /v3/api-docs e Swagger UI. Garantir schemas para respostas de sucesso e fallback.

## 13. Checklist de PR (template sugerido)
```
### Objetivo

### Alterações Principais

### Como Testar

### Evidências
(prints, logs)

### Checklist
- [ ] Tests passando
- [ ] Cobertura mantida/aumentada
- [ ] Sem TODOs críticos
- [ ] Sem segredos
- [ ] Documentado README / OpenAPI
```

## 14. Próximos Passos Automação
- Adicionar script para converter PlantUML em PNG em pipeline.
- Adicionar verificação de formatação (mvn spotless:check).

## 15. Sucesso
Quando for possível executar `docker compose up` e obter: API funcional + métricas + dashboard + frontend acessível.
