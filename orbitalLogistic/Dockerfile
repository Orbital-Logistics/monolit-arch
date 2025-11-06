FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY . .
# Run unit tests during image build (skip integration tests that require Docker-in-Docker)
# Integration tests should be run separately in CI/CD pipeline
RUN gradle unitTest --no-daemon && gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]