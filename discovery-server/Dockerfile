FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/discovery-server-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
