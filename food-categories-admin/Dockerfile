# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY build/libs/food-categories-admin-0.0.1-SNAPSHOT.jar app.jar

# Create a non-root user
RUN addgroup --system --gid 1001 springboot
RUN adduser --system --uid 1001 --gid 1001 springboot

# Change ownership of the app directory
RUN chown -R springboot:springboot /app
USER springboot

# Expose port
EXPOSE 8081

# Set the active profile to docker
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 