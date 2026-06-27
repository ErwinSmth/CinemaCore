# syntax=docker/dockerfile:1

# ================================
# Fase 1: Construccion (Quarkus)
# ================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copia el wrapper de Maven y dependencias primero para cache
COPY backend/movie-service/mvnw .
COPY backend/movie-service/.mvn .mvn
RUN chmod +x mvnw
COPY backend/movie-service/pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -q

# Copia el archivo de la API que Maven necesita para autogenerar clases
COPY docs/api-spec.yml /docs/api-spec.yml

# Evitar que Maven consuma toda la RAM durante la compilación
ENV MAVEN_OPTS="-Xmx256m -XX:TieredStopAtLevel=1"

# Copia el codigo y compila
COPY backend/movie-service/src src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -q

# ================================
# Fase 2: Ejecucion
# ================================
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# Crear usuario seguro
RUN addgroup -S quarkus && adduser -S quarkus -G quarkus

# Quarkus empaqueta la aplicacion en /target/quarkus-app/
COPY --from=builder --chown=quarkus:quarkus /app/target/quarkus-app/lib/ /app/lib/
COPY --from=builder --chown=quarkus:quarkus /app/target/quarkus-app/*.jar /app/
COPY --from=builder --chown=quarkus:quarkus /app/target/quarkus-app/app/ /app/app/
COPY --from=builder --chown=quarkus:quarkus /app/target/quarkus-app/quarkus/ /app/quarkus/

USER quarkus

EXPOSE 8082

# Tuning de RAM optimizado para entornos de escasos recursos (WSL2/Portafolios)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-Xms64m", \
    "-Xmx128m", \
    "-XX:+UseSerialGC", \
    "-XX:TieredStopAtLevel=1", \
    "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", \
    "-jar", "quarkus-run.jar"]
