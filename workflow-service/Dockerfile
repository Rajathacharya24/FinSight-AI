FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/workflow-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
