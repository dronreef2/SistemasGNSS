# 🎯 Issue: Implementar Melhorias de Alta e Média Prioridade

**Labels**: `enhancement`, `high-priority`, `observability`, `security`

## 📋 Descrição

Implementar as próximas melhorias críticas do GeoSat Gateway conforme roadmap estabelecido. Estas melhorias são essenciais para tornar o sistema production-ready.

## 🎯 Objetivos

- ✅ Observabilidade avançada com dashboards Grafana
- ✅ Segurança com validação de entrada
- ✅ Rastreabilidade com correlation IDs
- ✅ Métricas customizadas detalhadas
- ✅ CORS configurado adequadamente

## 📋 Tarefas

### 🏆 Alta Prioridade

#### 1. Grafana Dashboards JSON
- [ ] `observability/grafana/dashboards/overview-dashboard.json`
  - Request rate, latency, error rate, JVM metrics
- [ ] `observability/grafana/dashboards/resilience-dashboard.json`  
  - Circuit breaker state, retry count, failure patterns
- [ ] `observability/grafana/dashboards/cache-dashboard.json`
  - Hit rate, miss rate, Redis performance

**Métricas disponíveis**: `rbmc.requests.total`, `rbmc.requests.latency_seconds`, `rbmc.retries.total`, `rbmc.circuitbreaker.state`

#### 2. Validação @Valid nos Endpoints
- [ ] Criar `dto/RbmcSeriesRequest.java` com Bean Validation
  - Estação: `@Pattern(regexp = "^[A-Z]{4}$")`
  - Ano: `@Min(2000) @Max(2100)`
  - Dia: `@Min(1) @Max(366)`
  - Max: `@Min(1) @Max(10000)`
- [ ] Modificar `RbmcController.java` - adicionar `@Valid @ModelAttribute`
- [ ] Modificar `EstacaoController.java` - adicionar `@Valid @ModelAttribute`
- [ ] Expandir `GlobalExceptionHandler.java` - tratar `MethodArgumentNotValidException`
- [ ] Criar `RbmcControllerValidationTest.java`

#### 3. Correlation IDs (MDC)
- [ ] Criar `filter/CorrelationIdFilter.java`
  - Gerar UUID se header não existe
  - Adicionar ao MDC
  - Retornar header `X-Correlation-ID`
- [ ] Criar `util/CorrelationIdUtil.java` - helper para MDC
- [ ] Atualizar `application.yml` - logging pattern com `[%X{correlationId}]`
- [ ] Criar `CorrelationIdFilterTest.java`

### 🥈 Média Prioridade

#### 4. Custom Metrics Avançadas
- [ ] Criar `metrics/RbmcMetrics.java`
  - `rbmc.cache.hits` counter
  - `rbmc.cache.misses` counter  
  - `rbmc.requests.latency.by_station` timer com tag estacao
  - Método `getCacheHitRate()`
- [ ] Integrar em `RedisCacheService.java` - track hits/misses
- [ ] Integrar em `RbmcHttpClient.java` - latency por estação
- [ ] Criar `RbmcMetricsTest.java`

#### 5. CORS Configuration
- [ ] Criar `config/CorsConfig.java`
  - Allowed origins configurável via env
  - Métodos: GET, POST, PUT, DELETE, OPTIONS
  - Exposed headers: X-Correlation-ID, Retry-After
  - Credentials: true, Max-age: 3600
- [ ] Atualizar `application.yml` - properties CORS
- [ ] Criar `CorsConfigTest.java`

#### 6. Refatorar Testes com ResponseHandler
- [ ] Atualizar `RbmcHttpClientRetryTest.java` - usar `HttpClientResponseHandler`
- [ ] Atualizar `RbmcHttpClientCircuitBreakerTest.java` - usar `HttpClientResponseHandler`
- [ ] Remover todos os warnings de métodos deprecados

## 📂 Arquivos

### Novos (13 arquivos)
- 3 dashboards JSON
- 4 classes Java novas (DTO, Filter, Util, Metrics, Config)
- 6 testes novos

### Modificados (8 arquivos)
- 2 controllers
- 1 exception handler
- 2 services
- 1 application.yml
- 2 testes existentes

## 📚 Documentação de Referência

**Especificação Técnica Completa**: `docs/CODING_AGENT_SPEC.md`
- Código de exemplo completo
- Especificações técnicas detalhadas
- Estrutura exata dos arquivos
- Padrões de teste

**Resumo Executivo**: `docs/AGENT_DELEGATION_SUMMARY.md`

## ✅ Critérios de Aceitação

### Funcional
- [ ] Dashboards Grafana importam e funcionam
- [ ] Validação @Valid funciona em todos os endpoints
- [ ] Correlation IDs aparecem em logs e headers
- [ ] Métricas customizadas disponíveis no Prometheus
- [ ] CORS configurado e funcional

### Técnico
- [ ] Código compila sem erros
- [ ] Todos os testes passam
- [ ] Cobertura de testes >= 80%
- [ ] Zero warnings de deprecation
- [ ] Segue padrões do projeto

### Documentação
- [ ] Código bem comentado
- [ ] Javadoc nas classes públicas
- [ ] README atualizado se necessário

## 🎯 Definição de Pronto

- [ ] Código reviewed e aprovado
- [ ] Testes passando em CI/CD
- [ ] Dashboards testados no Grafana local
- [ ] Métricas validadas no Prometheus
- [ ] Documentação atualizada

## 📊 Impacto Esperado

- **Observabilidade**: +300% com dashboards e métricas
- **Segurança**: +200% com validação de entrada
- **Debugging**: +400% com correlation IDs
- **Frontend**: Habilitado com CORS
- **Qualidade**: Zero warnings de código

## 🔄 Dependências

- Spring Boot Validation starter (já incluído)
- Micrometer (já incluído)
- Grafana e Prometheus (via docker-compose)

---

**Branch**: `copilot/vscode1759331389769`  
**Commit Base**: `64d8a9e`  
**Estimativa**: 2-3 sprints (2-3 semanas)  
**Prioridade**: 🔴 Alta