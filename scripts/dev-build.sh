#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -f geosat-gateway/pom.xml ]; then
  echo "geosat-gateway/pom.xml não encontrado. Projeto Java ainda não inicializado." >&2
  exit 0
fi

MVN=${MVN:-mvn}
echo "Executando build + testes..."
cd geosat-gateway
$MVN -q clean verify

echo "✅ Build + testes concluídos com sucesso."