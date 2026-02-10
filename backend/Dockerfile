# Stage 1 : Build avec Maven
FROM maven:3.9.0-eclipse-temurin-17 AS builder

WORKDIR /build

# Copier les fichiers du projet
COPY pom.xml .
COPY src ./src

# Compiler et packager
RUN mvn clean package -DskipTests

# Stage 2 : Runtime avec JRE léger
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le JAR du stage de build
COPY --from=builder /build/target/carte-migration-1.0.0.jar app.jar

# Exposer le port
EXPOSE 8080

# Variables d'environnement par défaut
ENV DB_HOST=postgres \
    DB_PORT=5432 \
    DB_USER=postgres \
    DB_PASSWORD=kanto \
    DB_NAME=projetcloud \
    SERVER_PORT=8080

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
