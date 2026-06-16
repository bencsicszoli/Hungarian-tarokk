# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Dependency-k előtöltése a build cache kihasználásához
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Forráskód fordítása
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# --- Run stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
