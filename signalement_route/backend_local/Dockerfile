# Build stage
FROM openjdk:21 AS build
WORKDIR /app

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Cache dependencies
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=build /app/target/*.war app.war
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.war"]
