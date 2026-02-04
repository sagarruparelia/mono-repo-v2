FROM eclipse-temurin:25-jre-alpine@sha256:ddb04ab0b5e7167b5563da2f9724090bdd59c9e80e74c42f675ee88b2ab2cfc9

WORKDIR /app

# Copy pre-built JAR from CI (expect exactly one JAR)
COPY apps/bff/target/bff-*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
