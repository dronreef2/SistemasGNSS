<div align="center">
  <h1>🌐 GeoSat Gateway</h1>
  <p>Gateway unificado para dados GNSS da RBMC (IBGE): relatórios técnicos, arquivos RINEX2/3 e órbitas – com resiliência, métricas e base para visualizações.</p>
</div>

## 🎯 Objetivo
Fornecer uma API coesa e resiliente sobre os endpoints públicos da RBMC, adicionando:
* Fallback com cache (Redis)
* Métricas (Micrometer / Prometheus)
* Resiliência (Retry + CircuitBreaker via Resilience4j)
* Observabilidade estruturada
* Base para UI Web (mapa + download + visualizações)

## ✅ Status Atual
| Área | Situação |
|------|----------|
| Endpoints RBMC | relatorio, rinex2, rinex3 (1s/15s), orbitas (placeholders de metadata) |
| Cliente HTTP | Apache HttpClient5 + preparo para HTTP/2 (H2AsyncRequester) |
| Resiliência | Retry + CircuitBreaker + fallback 503 (Redis) |
| Cache | Metadados por chave + TTL (6h/12h) |
| OpenAPI | Anotações SpringDoc |
| Métricas | Contadores + timer + gauge estado CB |
| Testes | Unit + integração (Redis Testcontainers) |
| Streaming binário | Pendente (fase futura) |
| Persistência domínio | Não iniciada |
| Frontend | Não iniciado |

## 🧩 Arquitetura (Visão)
Arquivo PlantUML: `plantuml/architecture.puml`.

## 🚀 Endpoints Atuais
Base: `/api/v1/rbmc`

| Método | Rota | Descrição | Fallback 503 |
|--------|------|----------|-------------|
| GET | `/{estacao}/relatorio` | Metadados link relatório PDF | Sim |
| GET | `/{estacao}/rinex2/{ano}/{dia}` | RINEX2 15s (metadata placeholder) | Sim |
| GET | `/{estacao}/rinex3/1s/{ano}/{dia}/{hora}/{minuto}/{tipo}` | RINEX3 1s | Sim |
| GET | `/{estacao}/rinex3/{ano}/{dia}` | RINEX3 15s | Sim |
| GET | `/orbitas/{ano}/{dia}` | Órbitas multiconstelação | Sim |

Exemplo sucesso:
```json
{
  "estacao": "ALAR",
  "tipo": "pdf",
  "link": "https://servicodados.ibge.gov.br/api/v1/rbmc/relatorio/alar",
  "descricao": "Relatório técnico (placeholder)",
  "ultimaAtualizacao": "2025-09-29T15:00:00Z"
}
```
Fallback:
```json
{
  "estacao": "ALAR",
  "status": "indisponivel",
  "mensagem": "Falha temporária — a estação tirou uma soneca 🚀",
  "timestamp": "2025-09-29T15:10:00Z",
  "cache": {}
}
```

## ⚙️ Stack
| Categoria | Tecnologia |
|-----------|------------|
| Runtime | Java 17, Spring Boot 3 |
| HTTP Client | Apache HttpComponents 5 |
| Resiliência | Resilience4j |
| Cache | Redis |
| Observabilidade | Micrometer + Actuator |
| Docs | SpringDoc OpenAPI |
| Testes | JUnit 5, Mockito, Testcontainers |

## 🧪 Execução Local
Pré-requisitos: JDK 17+, Maven, Docker (opcional para Redis).
```bash
mvn -q -pl geosat-gateway spring-boot:run
```
Redis rápido:
```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```
Teste:
```bash
curl -s http://localhost:8080/api/v1/rbmc/ALAR/relatorio | jq
```
OpenAPI UI: `http://localhost:8080/swagger-ui.html`

## 🛡️ Resiliência
* Retry e CircuitBreaker programáticos (perfil de teste com tempos reduzidos).
* Fallback retorna HTTP 503 + corpo `RbmcFallbackResponse`.

## 📊 Métricas
| Nome | Tipo | Descrição |
|------|------|-----------|
| `rbmc.requests.total` | counter | Tentativas HTTP reais |
| `rbmc.requests.latency_seconds` | timer | Latência final por chamada |
| `rbmc.retries.total` | counter | Quantidade de retries efetuados |
| `rbmc.circuitbreaker.state` | gauge | Estado do CircuitBreaker |

## 🗺️ Roadmap (Próximas Fases)
Fase | Objetivo | Destaques
-----|----------|----------
1 | HTTP/2 toggle | Comparar latência, instrumentar
2 | Persistência Estações | PostgreSQL/PostGIS + Flyway
3 | Parser RINEX inicial | Header + contagem epochs
4 | Frontend Mapa | Leaflet + estação interativa
5 | Armazenamento Arquivos | MinIO + checksum
6 | Séries SNR/Posição | TimescaleDB
7 | Observabilidade avançada | Grafana + tracing OTel
8 | Segurança | API key / OAuth2 + rate limit
9 | Streaming binário | Proxy eficiente + ZIP batch

## 🧱 Estrutura
```
SistemasGNSS/
  geosat-gateway/
  plantuml/
  scripts/
  README.md
```

## 🧪 Testes
```bash
mvn -q -pl geosat-gateway test
```
Testes de integração usam Redis via Testcontainers.

## ADRs (Resumo)
| ID | Decisão | Status |
|----|---------|--------|
| ADR-001 | HttpComponents 5 | Aceita |
| ADR-002 | Resilience4j programático | Aceita |
| ADR-003 | Redis fallback | Aceita |
| ADR-004 | Fallback HTTP 503 | Aceita |
| ADR-005 | Metadados placeholders | Temporária |

## Melhorias Planejadas (Técnicas)
1. Métricas por endpoint (`rbmc.endpoint.requests{tipo=...}`)
2. Header `Retry-After` derivado do tempo restante OPEN
3. Parser incremental RINEX (streaming) + testes
4. Feature flag `rbmc.http2.enabled`
5. Observabilidade de bytes transferidos

## Licença
Pendente (sugestão: MIT ou Apache 2.0).

---
Contribuições e ideias são bem-vindas. ✨

