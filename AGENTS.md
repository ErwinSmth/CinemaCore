# AGENTS.md - Backend Cinestar

## Project Overview

Monorepo poliglota de microservicios para sistema de cine (Cinestar).
- **API Gateway**: Node.js + Express 5 + TypeScript
- **Auth Service**: Spring Boot 4 + Java 17 + JWT
- **Movie Service**: Quarkus + Java 17 + Panache
- **Showtime Service**: Quarkus + Java 17 + Panache
- **Seat Service**: Spring Boot 4 + Java 17 + Optimistic Locking
- **Database**: PostgreSQL 15 (puerto 5433), 4 databases: db_auth, db_movies, db_showtime, db_seats
- **Cache**: Redis
- **Infraestructura**: Docker Compose, Alpine Linux

## Monorepo Rules

1. **UN microservicio a la vez** - No leer/modificar archivos de otros servicios en la misma sesión
2. **No asumir dependencias** - Verificar pom.xml/package.json antes de usar librerías nuevas
3. **Migraciones Flyway** - Code-First, nombre: `V{version}__{description}.sql`
4. **100% REST** - Comunicación inter-servicio vía HTTP/HTTPS, sin mensajería asíncrona
5. **API Gateway como único punto de entrada** - Frontend solo habla con el Gateway

## Communication Pattern

```
Frontend → API Gateway → Microservicios
```

- API Gateway orquesta TODA la comunicación entre servicios
- JWT se valida en el API Gateway antes de reenviar a microservicios
- Cada microservicio tiene su propia base de datos (no compartidas)

## Security

- JWT con secret compartido entre Auth Service y API Gateway
- BCrypt para passwords
- RBAC: ROLE_CLIENTE, ROLE_ADMINISTRADOR, ROLE_TAQUILLERO
- Stateless sessions

## Code Style

- Sin comentarios a menos que se pida explícitamente
- Sin explicaciones a menos que se use `/explain`
- Retornar solo código modificado
- Tablas de datos en formato TOON o Markdown
