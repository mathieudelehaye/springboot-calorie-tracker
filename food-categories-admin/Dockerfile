# Multi-stage build for Spring Boot application
FROM gradle:8.14.1-jdk17 AS builder

# Set working directory
WORKDIR /app

# Copy gradle files for dependency caching
COPY settings.gradle ./
COPY gradle gradle
COPY gradlew* ./

# Copy all build.gradle files
COPY food-categories-admin/build.gradle food-categories-admin/
COPY shared/build.gradle shared/
COPY calorie-tracker-app/build.gradle calorie-tracker-app/

# Download dependencies (this layer will be cached if dependencies don't change)
RUN gradle food-categories-admin:build --no-daemon -x test || return 0

# Copy source code
COPY food-categories-admin/src food-categories-admin/src
COPY shared/src shared/src

# Build the application
RUN gradle food-categories-admin:bootJar --no-daemon

# Runtime stage - Using Eclipse Temurin (recommended OpenJDK distribution)
FROM eclipse-temurin:17-jre-jammy

# Create non-root user for security
RUN groupadd -r springboot && useradd -r -g springboot springboot

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/food-categories-admin/build/libs/*.jar app.jar

# Change ownership to springboot user
RUN chown springboot:springboot app.jar

# Switch to non-root user
USER springboot

# Expose port
EXPOSE 8080

# Set the active profile to docker
ENV SPRING_PROFILES_ACTIVE=docker

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 