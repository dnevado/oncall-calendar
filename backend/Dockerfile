# Backend de guardias (Spring Boot, Java 21). Imagen de dos etapas; se ejecuta sin root.
# Las pruebas no se lanzan aquí: necesitan Docker (Testcontainers) y se ejecutan con
# `mvn verify` antes de construir la imagen.

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21.0.5_11-jre-alpine
RUN addgroup -S guardias && adduser -S -G guardias guardias
WORKDIR /app
COPY --from=build /src/target/guardias-backend-*.jar app.jar
USER guardias
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
