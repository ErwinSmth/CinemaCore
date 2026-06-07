---
name: docker
description: Guía de Docker Compose, multi-stage builds, Alpine Linux para microservicios
---

# Docker Skill - Cinestar Backend

## Arquitectura

```
cinestar-infra/
├── docker-compose.yml
└── init-db/
    └── init.sql
```

## Docker Compose

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    container_name: cinestar-postgres
    ports:
      - "5433:5432"
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: root
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-db/init.sql:/docker-entrypoint-initdb.d/init.sql

volumes:
  postgres_data:
```

## Init SQL

```sql
CREATE DATABASE db_auth;
CREATE DATABASE db_movies;
CREATE DATABASE db_showtime;
CREATE DATABASE db_seats;
```

## Multi-Stage Build Patterns

### Java/Spring Boot
```dockerfile
# Builder
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runner
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms64m", "-Xmx128m", "-jar", "app.jar"]
```

### Java/Quarkus
```dockerfile
FROM quarkus/quarkus-maven-image AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S quarkus && adduser -S quarkus -G quarkus
USER quarkus
COPY --from=builder /app/target/quarkus-app/ /app/
EXPOSE 8080
ENTRYPOINT ["/app/run.sh"]
```

### Node.js/TypeScript
```dockerfile
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:22-alpine
RUN addgroup -S gateway && adduser -S gateway -G gateway
USER gateway
WORKDIR /app
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package*.json ./
RUN npm ci --omit=dev
EXPOSE 3000
HEALTHCHECK --interval=30s CMD wget -q --spider http://localhost:3000/health || exit 1
CMD ["node", "dist/index.js"]
```

## Convenciones

- **Base image**: Alpine para producción, JDK para builder
- **Non-root user**: Siempre crear usuario dedicado
- **Healthchecks**: Exponer endpoint `/health`
- **Memoria**: Limitar JVM heap (`-Xms64m -Xmx128m`)
- **.dockerignore**: Excluir `target/`, `node_modules/`, `.git/`, `*.env`

## Comandos Útiles

```bash
# Levantar infraestructura
docker-compose -f cinestar-infra/docker-compose.yml up -d

# Ver logs
docker-compose logs -f postgres

# Detener
docker-compose down

# Reconstruir sin cache
docker-compose build --no-cache
```
