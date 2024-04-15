# Phase 1: Build stage
FROM openjdk:11-jdk-slim AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew wrapper --gradle-version 8.2
RUN ./gradlew build

# Phase 2: Deploy App
FROM openjdk:11-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/MapEngine-1.0-SNAPSHOT.jar /app/map-engine.jar

CMD ["java", "-Xmx5g", "-jar", "/app/map-engine.jar"]

