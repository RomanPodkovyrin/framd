# ============================================================
# Stage 1: Build
# ============================================================
ARG JDK_IMAGE=eclipse-temurin:25-jdk-noble
ARG JRE_IMAGE=eclipse-temurin:25-jre-noble
#https://hub.docker.com/_/eclipse-temurin
#https://github.com/adoptium/temurin-build



FROM ${JDK_IMAGE} AS build
LABEL org.opencontainers.image.authors=romanempire.dev
WORKDIR /build

# Copy Maven wrapper and POM first (layer caching for dependencies)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached unless pom changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build
COPY src src
RUN ./mvnw package -DskipTests -B

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM ${JRE_IMAGE}
WORKDIR /app
LABEL org.opencontainers.image.authors=romanempire.dev

# Create directories for volumes
RUN mkdir -p /media /previews

# Copy JAR from build stage
COPY --from=build /build/target/Framd-0.0.1-SNAPSHOT.jar app.jar

#ENV JAVA_OPTS="-XX:MaxHeapFreeRatio=30 -XX:MinHeapFreeRatio=10"

ENV SPRING_PROFILES_ACTIVE="prod"
EXPOSE 7878

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -q -O- http://localhost:7878/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]