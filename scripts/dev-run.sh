#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "🚀 GeoSat Gateway - Execução Local Rápida"
echo ""

# Verificar se Redis está rodando
if ! nc -z localhost 6379 2>/dev/null; then
  echo "⚠️  Redis não detectado em localhost:6379"
  echo "   Iniciando Redis via Docker..."
  docker run -d --name geosat-redis -p 6379:6379 redis:7-alpine
  sleep 2
fi

echo "✅ Redis disponível"
echo ""

# Rodar aplicação
cd geosat-gateway

if [ "${1:-}" == "--build" ]; then
  echo "🔨 Compilando projeto..."
  mvn -q clean package -DskipTests
  echo ""
fi

echo "🌐 Iniciando GeoSat Gateway..."
echo "   Backend: http://localhost:8080"
echo "   Frontend: http://localhost:8080/app"
echo "   OpenAPI: http://localhost:8080/swagger-ui.html"
echo "   Metrics: http://localhost:8080/actuator/prometheus"
echo ""

mvn -q spring-boot:run
