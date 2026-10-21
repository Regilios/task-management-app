FROM eclipse-temurin:21-jdk-alpine@sha256:6ea5548706b60ac0a602eaf48af74792cbab012d90e811ca8db6184b16b5c3d6 AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY core core
COPY app app
RUN --mount=type=cache,target=/root/.m2 chmod +x mvnw && ./mvnw -B -ntp package

FROM eclipse-temurin:21-jre-alpine@sha256:974b08960c5d96694c780e65b2d5705268ab1e1ca1a0dd0caf4ba6c3fe34d699
WORKDIR /app
RUN addgroup -S stockflow && adduser -S stockflow -G stockflow
COPY --from=build --chown=stockflow:stockflow /workspace/app/target/stockflow.jar ./app.jar
USER stockflow
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
