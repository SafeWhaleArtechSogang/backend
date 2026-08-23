FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S safewhale && adduser -S safewhale -G safewhale \
    && mkdir -p /app/uploads && chown -R safewhale:safewhale /app
COPY --from=builder /workspace/build/libs/*.jar app.jar
USER safewhale
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
