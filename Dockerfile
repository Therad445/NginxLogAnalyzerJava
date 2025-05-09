FROM maven:3.9.6-eclipse-temurin-22 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ---
FROM eclipse-temurin:22-jre
WORKDIR /app
COPY target/nginx-log-analyzer-1.0.0-jar-with-dependencies.jar app.jar
EXPOSE 8080
ENTRYPOINT ["nginx-log-analyzer", "-jar", "app.jar"]
