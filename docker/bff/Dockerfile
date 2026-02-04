# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven wrapper and pom
COPY apps/bff/mvnw ./
COPY apps/bff/.mvn ./.mvn
COPY apps/bff/pom.xml ./

# Download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source
COPY apps/bff/src ./src

# Build
RUN ./mvnw package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run
ENTRYPOINT ["java", "-jar", "app.jar"]
