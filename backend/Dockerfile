FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S paissa && adduser -S paissa -G paissa
COPY --from=build /build/target/aggregator.jar app.jar
USER paissa
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
