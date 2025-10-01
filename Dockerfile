# Dockerfile Multi-Stage para GeoSat Gateway
# Otimizado para tamanho e segurança

# ================================
# Stage 1: Build
# ================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copiar apenas POM primeiro para cache de dependências
COPY geosat-gateway/pom.xml ./
RUN mvn dependency:go-offline -B

# Copiar código fonte e compilar
COPY geosat-gateway/src ./src
RUN mvn clean package -DskipTests -B

# ================================
# Stage 2: Runtime
# ================================
FROM eclipse-temurin:17-jre-alpine

# Metadados da imagem
LABEL org.opencontainers.image.title="GeoSat Gateway"
LABEL org.opencontainers.image.description="Gateway unificado para dados GNSS da RBMC"
LABEL org.opencontainers.image.version="0.1.0"
LABEL org.opencontainers.image.authors="GeoSat Team"

# Criar usuário não-root
RUN addgroup -S geosat && adduser -S geosat -G geosat

WORKDIR /app

# Copiar JAR da stage de build
COPY --from=builder /build/target/*.jar app.jar

# Copiar frontend estático
COPY frontend/web ./frontend/web

# Mudar ownership para usuário não-root
RUN chown -R geosat:geosat /app

# Configurar JVM para container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Trocar para usuário não-root
USER geosat

# Comando de inicialização
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
