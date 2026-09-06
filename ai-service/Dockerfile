# ───── build stage ─────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Copy parent pom + every module's pom so the reactor resolves
COPY pom.xml ./pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY document-service/pom.xml ./document-service/pom.xml
COPY ai-service/pom.xml ./ai-service/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY workflow-service/pom.xml ./workflow-service/pom.xml
COPY config-server/pom.xml ./config-server/pom.xml
COPY discovery-server/pom.xml ./discovery-server/pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml
# Only the service being built needs its source
COPY ai-service/src ./ai-service/src
RUN mvn -B -ntp -pl ai-service -am clean package -Dmaven.test.skip=true

# ───── runtime stage ─────
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl wget
WORKDIR /app
COPY --from=build /build/ai-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]

