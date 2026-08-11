FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY config-server/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
