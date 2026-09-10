# --- Etapa 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# 1. Compilar e instalar la librería común (wd-lib-common)
COPY wd-lib-common ./wd-lib-common
RUN mvn -f wd-lib-common/pom.xml clean install -DskipTests

# 2. Descargar dependencias del microservicio (Caché de capas)
COPY ms-reporting-analytics-service/pom.xml ./ms-reporting-analytics-service/
RUN mvn -f ms-reporting-analytics-service/pom.xml dependency:go-offline -B

# 3. Copiar código fuente y empaquetar el JAR
COPY ms-reporting-analytics-service/src ./ms-reporting-analytics-service/src
RUN mvn -f ms-reporting-analytics-service/pom.xml clean package -DskipTests

# --- Etapa 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/ms-reporting-analytics-service/target/*.jar app.jar

EXPOSE 9098
ENTRYPOINT ["java", "-jar", "app.jar"]
