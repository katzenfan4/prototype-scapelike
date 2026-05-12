FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
ARG SKIP_TESTS=true
RUN if [ "$SKIP_TESTS" = "true" ]; then mvn package -DskipTests -q; else mvn package -q; fi

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/scapelike-server.jar app.jar
EXPOSE 7070
ENTRYPOINT ["java", "-jar", "app.jar"]
