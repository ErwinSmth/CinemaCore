# syntax=docker/dockerfile:1

# ================================
# Fase 1: Construccion
# ================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copia el wrapper de Maven y le da permisos de ejecucion
COPY mvnw .
COPY .mvn .mvn
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Copia el pom.xml primero para aprovechar el cache de capas de Docker
# Si el pom.xml no cambia, Docker reutiliza esta capa sin re-descargar dependencias
COPY pom.xml .

# --mount=type=cache evita que Maven re-descargue dependencias al disco en cada build
# El cache persiste entre builds usando el daemon de BuildKit
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -q

# Evitar que Maven consuma toda la RAM durante la compilación
ENV MAVEN_OPTS="-Xmx256m -XX:TieredStopAtLevel=1"

# Copia el codigo fuente y compila omitiendo tests
COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -q

# ================================
# Fase 2: Ejecucion
# ================================
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# Crea un grupo y usuario no-root para ejecutar la aplicacion de forma segura
# Evita que el proceso tenga privilegios innecesarios dentro del contenedor
RUN addgroup -S spring && adduser -S spring -G spring

# Copia unicamente el jar generado desde la fase de construccion
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Cambia al usuario no-root antes de ejecutar
USER spring

EXPOSE 8080

# JAVA_OPTS optimizado:
# -XX:+UseContainerSupport  -> respeta los limites de memoria del contenedor
# -Xms64m                   -> heap inicial bajo para no desperdiciar RAM al arrancar
# -Xmx128m                  -> heap maximo estricto para no superar el limite de WSL2
# -XX:+UseSerialGC           -> GC de un solo hilo, minimo overhead, ideal para poca RAM
# -XX:TieredStopAtLevel=1   -> desactiva la compilacion JIT avanzada (C2), ahorra RAM a cambio de throughput
# -Djava.security.egd       -> acelera el arranque en contenedores sin dispositivo de entropia real
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-Xms64m", \
    "-Xmx128m", \
    "-XX:+UseSerialGC", \
    "-XX:TieredStopAtLevel=1", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]