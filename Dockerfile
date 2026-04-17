# ─────────────────────────────────────────────────────────────
#  Stage 1 — BUILD
#  Uses the official Maven image to compile and package the app.
#  The .m2 cache is kept in a named layer to speed up rebuilds.
# ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

# Copy dependency descriptors first for better layer caching:
# Maven only re-downloads deps when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source and build the fat jar (skip tests — CI runs them separately)
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress

# ─────────────────────────────────────────────────────────────
#  Stage 2 — RUNTIME
#  Minimal JRE Alpine image — no build tools, no source code.
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: run as a non-root user
RUN addgroup -S pingbot && adduser -S pingbot -G pingbot

WORKDIR /app

# Copy only the fat jar from the builder stage
COPY --from=builder /workspace/target/always-on-ping-bot-*.jar app.jar

# Ownership to the unprivileged user
RUN chown pingbot:pingbot app.jar

USER pingbot

# Expose Actuator + app port
EXPOSE 8080

# JVM flags:
#   -XX:+UseContainerSupport  — respect cgroup CPU/memory limits
#   -XX:MaxRAMPercentage=75   — use 75% of available container RAM for heap
#   -Djava.security.egd      — faster startup on containers (no /dev/random block)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
