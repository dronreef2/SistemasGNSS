# Dockerfile Multi-Stage para GeoSat Gateway
# Otimizado para tamanho, clareza e segurança

# ======= STAGE 1: Build (maven builder)
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copiar apenas POM primeiro para cache de dependências
COPY geosat-gateway/pom.xml ./
RUN mvn dependency:go-offline -B

# Copiar código fonte e compilar
COPY geosat-gateway/src ./src
RUN mvn clean package -DskipTests -B

# ======= STAGE 2: Runtime (thin JRE image)
FROM eclipse-temurin:17-jre-alpine AS runtime

# Instala utilitários necessários para healthchecks (curl) e limpeza
RUN apk add --no-cache --no-progress curl \
  && rm -rf /var/cache/apk/* || true

# Copiar apenas o jar resultante do build stage
COPY --from=builder /build/target/*.jar /app/app.jar

# Criar usuário não-root antes de definir WORKDIR
RUN addgroup -S geosat && adduser -S geosat -G geosat

WORKDIR /app
USER geosat

# Configurar JVM para o container (padrões ajustáveis por env)
# NOTE: define JAVA_OPTS without surrounding quotes so expansion preserves leading '-'
ENV JAVA_OPTS=-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC

# Porta que a aplicação usa
EXPOSE 8080

# Healthcheck: usar curl e dar a aplicação mais tempo para iniciar
# - aumenta timeout/start-period e falha com códigos não-2xx
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=5 \
  CMD curl -fsS --max-time 4 http://127.0.0.1:8080/actuator/health || exit 1

# Entrada padrão: usar exec para que java receba os argumentos corretamente
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
