# Stage 1: Build the Spring Boot application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy Maven descriptor and source code
COPY pom.xml .
COPY src ./src

# Build production jar skipping test execution during container build
RUN mvn clean package -DskipTests

# Stage 2: Minimal Java 21 runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=build /app/target/*.jar app.jar

# Render provides the PORT environment variable dynamically
ENV PORT=8080
EXPOSE 8080

# Execute Spring Boot passing the assigned port
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
