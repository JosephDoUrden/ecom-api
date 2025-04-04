# Build stage
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# Download all required dependencies
RUN mvn dependency:go-offline -B
COPY src src
RUN mvn package -DskipTests

# Final stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create app user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create directories
RUN mkdir -p /app/logs && \
  mkdir -p /app/config && \
  chown -R appuser:appgroup /app

# Copy the JAR file from build stage
COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/application.yml /app/config/

# Set up entrypoint script with default JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/"
ENV SPRING_PROFILES_ACTIVE="prod"
ENV SPRING_CONFIG_LOCATION="classpath:/,file:/app/config/"

# Expose application port
EXPOSE 8080

# Switch to non-root user
USER appuser

# Set the entrypoint
ENTRYPOINT exec java $JAVA_OPTS -jar /app/app.jar
