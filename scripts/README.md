# Scripts de Desenvolvimento

Este diretório contém scripts utilitários para facilitar o desenvolvimento, build e testes do projeto GeoSat Gateway.

## Scripts Disponíveis

### `dev-run.sh`
Execução rápida local da aplicação com verificação automática de Redis.

**Uso:**
```bash
./scripts/dev-run.sh           # Rodar sem rebuild
./scripts/dev-run.sh --build   # Compilar antes de rodar
```

**O que faz:**
- Verifica se Redis está disponível (inicia container se necessário)
- Inicia aplicação Spring Boot
- Exibe URLs úteis (backend, frontend, OpenAPI, metrics)

### `dev-build.sh`
Build completo + testes do projeto Java.

**Uso:**
```bash
./scripts/dev-build.sh
```

**O que faz:**
- Executa `mvn clean verify` na pasta `geosat-gateway/`
- Roda todos os testes (unit + integração)
- Gera relatório de cobertura (Jacoco)

### `gen-plantuml-png.sh`
Gera diagramas PNG a partir dos arquivos `.puml`.

**Uso:**
```bash
./scripts/gen-plantuml-png.sh
```

**Requisitos:**
- Docker (recomendado) ou
- Java + plantuml.jar (baixado automaticamente se necessário)

**O que faz:**
- Processa todos os `.puml` em `plantuml/`
- Gera PNGs no mesmo diretório

### `run-e2e.sh`
Testes End-to-End com docker-compose (quando disponível).

**Uso:**
```bash
./scripts/run-e2e.sh
```

**O que faz:**
- Sobe stack completa via `docker-compose up`
- Aguarda aplicação ficar saudável
- Executa testes básicos de endpoints
- Derruba stack ao finalizar

**Status:** Placeholder - será expandido quando `docker-compose.yml` estiver completo.

## Estrutura Complementar

Veja também:
- `dev-tools.md` - Guia detalhado de ferramentas, padrões e fluxo de PRs
- `../plantuml/` - Diagramas de arquitetura
- `../README.md` - Documentação principal do projeto

## Dicas

**Desenvolvimento rápido:**
```bash
# Terminal 1: Redis
docker run -d --name geosat-redis -p 6379:6379 redis:7-alpine

# Terminal 2: Backend
./scripts/dev-run.sh

# Terminal 3: Testes
./scripts/dev-build.sh
```

**Atualizar diagrama:**
```bash
# Editar plantuml/architecture.puml
vim plantuml/architecture.puml

# Regenerar PNG
./scripts/gen-plantuml-png.sh

# Commit ambos
git add plantuml/
git commit -m "docs: atualiza diagrama de arquitetura"
```

**CI Local (simulação):**
```bash
./scripts/dev-build.sh && \
./scripts/gen-plantuml-png.sh && \
echo "✅ Pipeline local OK"
```
