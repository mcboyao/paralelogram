# Build Stage
FROM gradle:jdk17-corretto-al2023 AS build-stage

COPY build.gradle /app/
COPY gradlew /app/
COPY settings.gradle /app/
COPY gradle /app/gradle
COPY src /app/src
WORKDIR /app
RUN ./gradlew clean bootJar

# RUN ls -l /app/build/

# Run Stage
FROM openjdk:17-jdk-slim AS run-stage

WORKDIR /app

COPY --from=build-stage /app /app/build/
COPY --from=build-stage /app/build/libs/backend-user-*.jar /app/backend-user-service.jar

# Expose the application port
EXPOSE 8091

# Run the application
ENTRYPOINT ["java", "-jar", "/app/backend-user-service.jar"]

