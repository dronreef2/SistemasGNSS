#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

# Gera PNG(s) a partir de arquivos .puml em plantuml/ usando container oficial ou jar local.
# Requisitos: docker ou plantuml.jar + java.

PUML_DIR="plantuml"
OUT_DIR="plantuml"

mkdir -p "$OUT_DIR"

convert_with_docker() {
  echo "[INFO] Gerando PNGs via Docker..."
  docker run --rm -v "$(pwd)/$PUML_DIR":/workspace plantuml/plantuml -tpng /workspace/*.puml
}

convert_with_jar() {
  if [ ! -f plantuml.jar ]; then
    echo "[INFO] Baixando plantuml.jar..." >&2
    curl -L -o plantuml.jar https://github.com/plantuml/plantuml/releases/latest/download/plantuml.jar
  fi
  echo "[INFO] Gerando PNGs via plantuml.jar..."
  java -jar plantuml.jar -tpng "$PUML_DIR"/*.puml
}

if command -v docker &>/dev/null; then
  convert_with_docker
else
  if ! command -v java &>/dev/null; then
    echo "[ERROR] Docker e Java não encontrados. Instale um deles." >&2
    exit 1
  fi
  convert_with_jar
fi

echo "✅ Diagramas gerados em $OUT_DIR"