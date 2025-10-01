#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

# Script base para (futuramente) executar testes E2E após docker-compose subir serviços.
# Placeholder: ajustado quando docker-compose.yml existir.

COMPOSE_FILE="docker-compose.yml"

if [ ! -f "$COMPOSE_FILE" ]; then
  echo "[WARN] docker-compose.yml não encontrado ainda. Este script será útil quando a stack completa estiver definida." >&2
  exit 0
fi

echo "[INFO] Subindo stack..."
docker compose up -d --build

cleanup() {
  echo "[INFO] Derrubando stack..."
  docker compose down -v
}
trap cleanup EXIT

echo "[INFO] Aguardando aplicação (timeout 60s)..."
for i in {1..60}; do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "✅ Aplicação saudável."
    break
  fi
  sleep 1
  if [ "$i" -eq 60 ]; then
    echo "[ERROR] Timeout esperando /actuator/health" >&2
    exit 2
  fi
done

# Exemplos de checagens básicas (ajustar quando endpoints prontos)
echo "[INFO] Testando endpoints..."
set +e
echo "- GET /api/v1/rbmc/ALAR/relatorio"
curl -fsS "http://localhost:8080/api/v1/rbmc/ALAR/relatorio" >/dev/null && echo "  ✅ OK" || echo "  ❌ FAIL"

echo "- GET /api/v1/estacoes"
curl -fsS "http://localhost:8080/api/v1/estacoes" >/dev/null && echo "  ✅ OK" || echo "  ❌ FAIL"

echo "- GET /actuator/prometheus"
curl -fsS "http://localhost:8080/actuator/prometheus" >/dev/null && echo "  ✅ OK" || echo "  ❌ FAIL"
set -e

echo "✅ [E2E] Finalizado (placeholder - expandir conforme necessário)."