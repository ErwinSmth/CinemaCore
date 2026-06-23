# Microservicios (Backend Cinestar)

## 1. Visión General de los Servicios
Basado en las reglas del Monorepo (`AGENTS.md`) y la documentación funcional, el backend se orquesta mediante un Gateway y cuatro servicios Core.

| Servicio | Responsabilidad | Stack Tecnológico | Base de Datos |
|---|---|---|---|
| **API Gateway** | Orquestación, validación JWT, ruteo. | Node.js + Express 5 + TS | N/A |
| **Auth Service** | Login, registro, RBAC, firma JWT. | Spring Boot 4 + Java 17 | `db_auth` (PostgreSQL) |
| **Movie Service** | Catálogo, multimedia, sinopsis. | Quarkus + Java 17 + Panache | `db_movies` (PostgreSQL) |
| **Showtime Service** | Horarios, salas, formatos (2D/3D). | Quarkus + Java 17 + Panache | `db_showtime` (PostgreSQL) |
| **Seat Service** | Estado de butacas, compras, optimistic locking. | Spring Boot 4 + Java 17 | `db_seats` (PostgreSQL) |

### Notas de Implementación (Migraciones)
* Todas las migraciones usarán **Flyway** (Code-First), con nomenclatura estricta: `V{version}__{description}.sql`.
* Cada servicio es independiente, las peticiones transitan 100% por API REST sin mensajería asíncrona por Kafka/RabbitMQ.

*(En esta carpeta irán los documentos detallados para cada servicio: endpoints, DTOs y diagramas de clases específicos).*
