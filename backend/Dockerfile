# ──────────────────────────────────────────────────────────────
# Production image — multi-stage build.
#
# Layer cache strategy (top = slowest-changing, bottom = fastest-changing):
#   1. Base image         — changes only on JDK security releases
#   2. Maven wrapper      — changes only on wrapper upgrades
#   3. pom.xml + offline  — changes only when deps change
#   4. src/               — changes on every commit  ← cache busts here
# ──────────────────────────────────────────────────────────────

# ── Stage 1: build ──────────────────────────────────────────
FROM eclipse-temurin:21.0.7_6-jdk-alpine AS build
WORKDIR /app

# [SLOW] Maven wrapper — rarely changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# [SLOW] Dependency download — only re-runs when pom.xml changes
RUN ./mvnw dependency:go-offline -B

# [FAST] Source — changes on every commit; intentionally last
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ── Stage 2: runtime ────────────────────────────────────────
FROM eclipse-temurin:21.0.7_6-jre-alpine
WORKDIR /app

# [SLOW] Non-root user setup — never changes
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# [FAST] Artifact from build stage
COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8085}/actuator/health || exit 1

# Railway injects PORT; Spring Boot reads CONTAINER_PORT (default 8085)
ENTRYPOINT ["java", "-jar", "app.jar"]
