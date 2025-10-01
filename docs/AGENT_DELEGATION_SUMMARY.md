# 🎯 DELEGAÇÃO AO AGENTE DE CODIFICAÇÃO

## RESUMO EXECUTIVO

Implementar 6 melhorias críticas no GeoSat Gateway conforme `docs/CODING_AGENT_SPEC.md`.

---

## 📋 TAREFAS

### ✅ TAREFA 1: Grafana Dashboards (3 arquivos JSON)

**Criar**:
- `observability/grafana/dashboards/overview-dashboard.json`
- `observability/grafana/dashboards/resilience-dashboard.json`  
- `observability/grafana/dashboards/cache-dashboard.json`

**Métricas a usar**:
- `rbmc.requests.total`
- `rbmc.requests.latency_seconds`
- `rbmc.retries.total`
- `rbmc.circuitbreaker.state`

---

### ✅ TAREFA 2: Validação @Valid

**Criar**:
- `dto/RbmcSeriesRequest.java` com validações Bean Validation

**Modificar**:
- `RbmcController.java` - adicionar `@Valid @ModelAttribute`
- `EstacaoController.java` - adicionar `@Valid @ModelAttribute`
- `GlobalExceptionHandler.java` - tratar `MethodArgumentNotValidException`

**Criar teste**:
- `RbmcControllerValidationTest.java`

---

### ✅ TAREFA 3: Correlation IDs

**Criar**:
- `filter/CorrelationIdFilter.java` - gera UUID, adiciona ao MDC, retorna header
- `util/CorrelationIdUtil.java` - helper para MDC

**Modificar**:
- `application.yml` - logging pattern com `[%X{correlationId}]`

**Criar teste**:
- `CorrelationIdFilterTest.java`

---

### ✅ TAREFA 4: Custom Metrics

**Criar**:
- `metrics/RbmcMetrics.java` - cache hits/misses, latency por estação

**Modificar**:
- `RedisCacheService.java` - track hits/misses
- `RbmcHttpClient.java` - latency por estação

**Criar teste**:
- `RbmcMetricsTest.java`

---

### ✅ TAREFA 5: CORS Config

**Criar**:
- `config/CorsConfig.java` - configuração CORS completa

**Modificar**:
- `application.yml` - properties CORS

**Criar teste**:
- `CorsConfigTest.java`

---

### ✅ TAREFA 6: Refatorar Testes

**Modificar**:
- `RbmcHttpClientRetryTest.java` - usar `HttpClientResponseHandler`
- `RbmcHttpClientCircuitBreakerTest.java` - usar `HttpClientResponseHandler`

**Objetivo**: Remover warnings de métodos deprecados

---

## ✅ CRITÉRIOS DE SUCESSO

- [ ] Código compila sem erros
- [ ] Todos os testes passam
- [ ] Zero warnings deprecation
- [ ] Cobertura >= 80%
- [ ] Dashboards importam no Grafana
- [ ] Correlation IDs em logs
- [ ] Métricas no Prometheus
- [ ] CORS funcional

---

## 📚 REFERÊNCIA COMPLETA

Ver `docs/CODING_AGENT_SPEC.md` para:
- Código completo de exemplo
- Especificações técnicas detalhadas
- Estrutura dos arquivos
- Padrões a seguir

---

## 🎯 ORDEM DE IMPLEMENTAÇÃO

1. Correlation IDs (base)
2. Validação @Valid (segurança)
3. Custom Metrics (observabilidade)
4. CORS (frontend)
5. Dashboards (visualização)
6. Refatoração testes (limpeza)

---

**Branch**: `copilot/vscode1759331389769`
**Commit Base**: `6180785`
**Especificação**: `docs/CODING_AGENT_SPEC.md`
