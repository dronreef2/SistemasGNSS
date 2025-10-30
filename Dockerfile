# Dockerfile Multi-Stage para GeoSat Gateway
# Otimizado para tamanho e segurança
#
# ======= STAGE 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copiar apenas POM primeiro para cache de dependências
COPY geosat-gateway/pom.xml ./
RUN mvn dependency:go-offline -B

# Copiar código fonte e compilar
COPY geosat-gateway/src ./src
RUN mvn clean package -DskipTests -B

# ======= STAGE 2: Runtime
FROM eclipse-temurin:17-jre-alpine AS runtime

# Instala utilitários necessários para healthchecks (curl)
RUN apk add --no-cache curl

# Copiar apenas o jar resultante do build stage
COPY --from=builder /build/target/*.jar app.jar

# Configurar JVM para o container (exemplo)
ENV JAVA_OPTS=-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC

# Expor porta
EXPOSE 8080

# Healthcheck: usar curl (mais comum na Alpine) e dar start-period maior
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Usuário não-root
RUN addgroup -S geosat && adduser -S geosat -G geosat
WORKDIR /app
USER geosat

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app.jar"]
