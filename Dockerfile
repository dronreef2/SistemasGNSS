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

# Configurar JVM padrão (mantemos uma variável para sobrescrita se quiser)
ENV JAVA_OPTS_OVERRIDE=""

# Porta que a aplicação usa
EXPOSE 8080

# Healthcheck continua inalterado
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=5 \
  CMD curl -fsS --max-time 4 http://127.0.0.1:8080/actuator/health || exit 1

# Entrada padrão (exec form) — passa flags da JVM explicitamente para evitar parsing ambíguo
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "/app/app.jar"]
