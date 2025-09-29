# Especificação Interna (Histórico)

Conteúdo original do README preservado para referência de requisitos, escopo e metas iniciais.

---

Fase 3 – Seleção de Data (Ano + Dia Juliano)
Frontend

Inputs: ano (YYYY), dia (DDD).
Validação: ano >= 1995 && ano <= anoAtual; dia 001–365 (366 se ano bissexto).
Helper: botão “Hoje” e “Ontem” calculando DDD.
Função util: converter Date → dia juliano (JS). Aceitação
Inputs inválidos mostram mensagem e desabilitam botões de ação.


Fase 4 – Botões de Download (Relatório / RINEX)
Frontend

Botões: “Baixar Relatório” e “Baixar RINEX2 (15s)”.
O RINEX3 (1s) pode ficar em “Avançado” inicialmente.
Links diretos construídos usando padrão backend (/api/v1/rbmc/...). Backend
Confirmar que endpoints atuais retornam metadados (já há placeholders). Melhorias
Adicionar spinner enquanto tenta HEAD (opcional). Aceitação
Clique abre nova aba ou inicia download sem quebrar a página.


Fase 5 – Painel de Metadados
Frontend

Após seleção de estação → fetch /api/v1/estacoes/{codigo}/metadados.
Render: receptor, antena, altura, ultAtualização, status (derivado: se última atualização < X horas). Cache
Memoizar por estação em state.metadados[codigo]. Aceitação
Mudar de estação e voltar não refaz a chamada (cache hit).


Fase 6 – Gráficos (SNR & Trajetória Simplificada)
Backend

Preencher endpoints de SNR e posições com dados sintéticos plausíveis (até processamento real). Frontend
Introduzir Chart.js (mais simples que D3 para início).
Gráfico 1: SNR vs tempo (linhas ou pontos).
Gráfico 2: Dispersão de coordenadas (lat vs lon offset ou ENU local). Estrutura
js/charts.js
Adicionar função rebuildCharts(estacao, ano, dia). Aceitação
Placeholder exibe dados e reage à troca de data.


Fase 7 – Camadas Avançadas (Órbitas / Trajetória / Heatmaps)
Opções

Camada de trajetória diária: polilinha conectando posições.
Camada heatmap (leaflet.heat) com densidade de observações (futuro).
Camada órbitas (se backend fornecer orbita simplificada). Backend
Endpoint: /api/v1/orbitas/{ano}/{dia}?formato=geojson (placeholder). Aceitação
Toggle na UI ativa/desativa camadas.
Fase 8 – UX, Performance e Robustez
Melhorias

Skeleton loading para painel.
Debounce em inputs de data.
Estratégia de retry no frontend (ex: fetchRetry com backoff para SNR).
Expor tempo de resposta (X-Response-Time) e mostrar no debug.
LocalStorage para lembrar última estação e data.
Dark mode toggle (opcional). Aceitação
Recarregar página mantém contexto.
Fase 9 – Empacotamento / Deploy
Adicionar script npm (se optar depois por build minimizador).
Servir conteúdo estático direto do Spring (ResourceHandler apontando para /frontend/web).
Adicionar a pipelines (verificar integridade: HTML lint + link check).
Opcional: migrar para Vite se a complexidade crescer.
Estrutura de Pastas Proposta (Inicial)
frontend/web/ index.html css/ styles.css js/ app.js (boot principal) state.js (objeto de estado + eventos) apiClient.js (funções fetch) map.js (Leaflet init) ui.js (manipulação de DOM para dropdown, inputs, painel) charts.js (gráficos Chart.js) utils.js (datas, formatadores) assets/ icons/ station-online.png station-offline.png logo.svg

Contratos de API (Sugestões)
GET /api/v1/estacoes [ { "codigo":"ALAR", "nome":"Alagoinhas", "latitude": -12.135, "longitude": -38.423, "status":"ONLINE" // opcional, derivado } ]

GET /api/v1/estacoes/geojson { "type":"FeatureCollection", "features":[ {"type":"Feature","geometry":{"type":"Point","coordinates":[-38.423,-12.135]},"properties":{"codigo":"ALAR","nome":"Alagoinhas"}} ] }

GET /api/v1/estacoes/{codigo}/metadados { "codigo":"ALAR", "receptor":"TRIMBLE NETR9", "antena":"TRM59800.00"," altura_m":1.234, "ultimaObservacao":"2025-09-29T12:34:00Z" }

GET /api/v1/estacoes/{codigo}/snr?ano=2025&dia=272 { "codigo":"ALAR", "ano":2025, "dia":272, "samples":[ {"epoch":"2025-09-29T00:00:00Z","sv":"G01","snr":45.2}, ... ] }

GET /api/v1/estacoes/{codigo}/posicoes?ano=2025&dia=272 { "codigo":"ALAR", "referencia":"ITRF2014", "samples":[ {"epoch":"2025-09-29T00:00:00Z","lat":-12.135012,"lon":-38.423004,"h":423.12}, ... ] }

(Placeholder pode limitar a 100–300 pontos)

Organização do Código Frontend (Padrão Simples)
Cada módulo expõe funções nomeadas.
app.js orquestra: initState → initMap → loadStations → bindEvents.
state.js: const state = { estacoes: [], estacaoSelecionada: null, data: { ano:null, dia:null }, metadados: {}, snrCache: {}, posicoesCache: {} } Funções: setEstacao(), setData(), notify(eventName)
apiClient.js inclui fetchJson(url) + funções específicas (getEstacoes, getMetadados, getSnr, getPosicoes).
ui.js atualiza DOM (evitar lógica de dados ali).
Eventos Chave (Simples Pub/Sub)
station:selected
date:changed
data:loaded:metadados
data:loaded:snr
data:loaded:posicoes
Validações Data
Função isLeapYear(ano)
Limite dia = 366 se leap
Conversão Date → diaJuliano e vice-versa (para quick picks)
Métricas Futuras (Frontend Observability – opcional)
Contar latências de fetch (enviar via /metrics/custom?event=...).
Tracking de erros para console (Sentry opcional).
Riscos e Mitigações
Risco	Mitigação
API real lenta/intermitente	Cache backend + fallback + spinner claro
Crescimento JS desorganizado	Modularizar cedo + convenções de nomes
SNR/posições reais grandes	Paginação/decimação no backend (downsample)
Coordenadas não normalizadas	Definir logo sistema referência (lat/lon WGS84)
CORS bloqueado	Config global WebMvcConfigurer
Critérios de Aceite (Macro)
Mapa carrega todas as estações < 2s com rede normal.
Seleção de estação dispara carregamento de metadados + gráficos sem reload.
Data inválida não permite downloads.
Downloads funcionam para relatório e RINEX2.
Gráficos renderizam com placeholders sem travar UI.
Nenhum erro uncaught no console em fluxo básico.
Próximo Passo Recomendado Agora
Implementar Fase 0 (endpoints base + CORS) e criar scaffold da pasta frontend com arquivos vazios + index.html minimamente funcional.

Se quiser, já posso gerar o scaffold inicial e os endpoints placeholder. Confirma que avançamos para criação dos arquivos (Fase 0 + 1)? Diga “seguir” e implemento direto. Quer ajustar algo antes?

GPT-5 • 0.9x

____________________________________________________
```
(Seguem linhas originais)
SYSTEM:
Você é um agente de engenharia de software e DevOps especializado em criar micro-serviços robustos e produção-ready usando Java (Spring Boot), Apache HttpComponents Core, Resilience4j, Micrometer/Prometheus, Docker e Kubernetes. Seu objetivo é projetar, implementar, testar, containerizar e documentar o projeto "GeoSat Gateway" — um gateway que consulta a API RBMC do IBGE (relatórios PDF, arquivos RINEX2/3, órbitas) e expõe uma API unificada e uma UI visual e cômica. Sempre gere artefatos prontos para rodar localmente e para CI/CD.

DEVELOPER_INSTRUCTIONS:
- Linguagem principal: Java 17+ com Spring Boot 3.x (Maven preferido, Gradle aceitável).
- http client: Apache HttpComponents Core (não usar apenas RestTemplate/WebClient para chamadas externas; raciocine em wrapper sobre HttpComponents).
- Resiliência: Resilience4j para circuit breaker, retry (configurável) e bulkhead (opcional).
- Cache: Redis para cache de fallback (docker-compose para local).
- Observabilidade: Micrometer + /actuator/prometheus + Prometheus + Grafana (dashboards JSON).
- Logging: SLF4J + Logback, logs estruturados em JSON com traceId e campos padronizados.
- Testes: JUnit 5 + Mockito; testes de integração com Testcontainers ou docker-compose (mínimo).
- CI: GitHub Actions: build → tests → static analysis → build/push Docker image → deploy (instruções).
- Qualidade: aplicar Checkstyle/Spotless e cobertura mínima alvo (ex.: 70–85%).
- Segurança: não commitar segredos; usar env vars; validar inputs; rate limit simples por usuário/IP.
- Commits: pequenos commits coesos; um bom README com instruções para rodar localmente.
- Entregáveis: código, OpenAPI (openapi.yaml), docker-compose, k8s manifests (deployment + service), prometheus config, grafana dashboard JSON, README, diagrama de arquitetura (PlantUML + PNG), scripts de teste e postman collection.

USER_GOAL:
Construir um micro-serviço que:
1. Unifica chamadas às APIs RBMC (IBGE) e entrega respostas legíveis (JSON) com links para PDF / RINEX / Orbitas.
2. Implementa retries com backoff exponencial, circuit breaker com fallback (cache Redis) e logs/metrics.
3. Exponha uma UI web simples (React) com visual divertido (estação como personagem) e um endpoint REST documentado via OpenAPI.
4. Tenha pipelines de CI e infrastructure-as-code mínima para deploy.

(CONTINUA... ver README antigo para demais seções caso necessário)
```
