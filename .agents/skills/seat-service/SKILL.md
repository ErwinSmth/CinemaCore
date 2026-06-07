---
name: seat-service
description: Patrón de pre-generación de tickets, optimistic locking, comunicación inter-servicio vía API Gateway
---

# Seat Service Skill - Cinestar Backend

## Stack

- Spring Boot 4.0.6
- Java 17
- Spring Data JPA + Hibernate
- PostgreSQL 15 (db_seats)
- Flyway (Code-First)
- Optimistic Locking (@Version)
- Lombok

## Arquitectura de Comunicación

```
Frontend → API Gateway → Seat Service
                         ├── Showtime Service (validar función)
                         └── Auth Service (validar usuario)
```

**Regla**: TODA la comunicación entre servicios pasa por el API Gateway.

## Tablas

### asientos
```sql
CREATE TABLE asientos(
  asiento_id BIGSERIAL PRIMARY KEY,
  sala_id BIGINT NOT NULL,
  fila_butaca VARCHAR(5) NOT NULL,
  numero_butaca INT NOT NULL,
  tipo VARCHAR(20) NOT NULL
);
```

### tickets
```sql
CREATE TABLE tickets(
  ticket_id BIGSERIAL PRIMARY KEY,
  funcion_id BIGINT NOT NULL,
  asiento_id BIGINT NOT NULL REFERENCES asientos(asiento_id),
  usuario_id BIGINT,
  status VARCHAR(20) DEFAULT 'AVAILABLE',
  locked_until TIMESTAMP,
  version INT DEFAULT 0
);
```

## Ciclo de Vida del Ticket

### 1. Pre-generación (cuando Admin crea función)
- Los tickets se crean en bloque al programar una función
- Si la Sala 1 tiene 100 butacas → 100 filas en `tickets`
- Estado inicial: `status='AVAILABLE'`, `usuario_id=NULL`, `version=0`

### 2. Bloqueo (usuario selecciona asiento)
- `UPDATE tickets SET status='LOCKED', locked_until=now()+5min, version=version+1 WHERE asiento_id=X AND version=current`
- Si rows affected = 0 → `OptimisticLockException` → "Asiento tomado"
- Si rows affected = 1 → Éxito

### 3. Confirmación (usuario paga)
- `UPDATE tickets SET status='SOLD', usuario_id=Y WHERE asiento_id=X AND status='LOCKED' AND locked_until > now()`

### 4. Abandono (timeout expira)
- Job scheduler: `UPDATE tickets SET status='AVAILABLE', usuario_id=NULL, locked_until=NULL WHERE status='LOCKED' AND locked_until < now()`

## Columnas Especiales

| Columna | Tipo | Propósito |
|---------|------|-----------|
| `status` | VARCHAR(20) | AVAILABLE / LOCKED / SOLD (para Frontend) |
| `version` | INT | Optimistic Locking (para BD, invisible al usuario) |
| `locked_until` | TIMESTAMP | Temporizador de abandono (5 min) |

## Analogía: Status vs Version

- `status` = Letrero de plástico por fuera ("Libre / Ocupado") → Lo ve el Frontend
- `version` = Cerradura de metal por dentro → Evita choques en la BD

## Escenario de Colisión (Juan vs María)

```
1. Juan: UPDATE tickets SET status='LOCKED', version=2 WHERE asiento_id=50 AND version=1
   → Rows: 1 → Éxito

2. María: UPDATE tickets SET status='LOCKED', version=2 WHERE asiento_id=50 AND version=1
   → Rows: 0 → OptimisticLockException → "Asiento tomado"
```

## Endpoints (a definir posteriormente)

Pendiente de definición por el usuario.

## Comunicación con Otros Servicios

### Seat → Showtime (via API Gateway)
- Validar que la función existe y está activa
- Obtener duración de la película para `locked_until`

### Seat → Auth (via API Gateway)
- Validar `usuario_id` existe
- Verificar token JWT

## Estructura del Proyecto

```
servicios/seat-service/
├── src/main/java/pe/edu/utp/cinestar/seat_service/
│   ├── SeatServiceApplication.java
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   └── SeatController.java
│   ├── model/
│   │   ├── dto/
│   │   │   ├── LockRequest.java
│   │   │   ├── PreGenerateRequest.java
│   │   │   └── TicketResponse.java
│   │   └── entity/
│   │       ├── AsientoEntity.java
│   │       └── TicketEntity.java
│   ├── repository/
│   │   ├── AsientoRepository.java
│   │   └── TicketRepository.java
│   ├── service/
│   │   └── SeatService.java
│   ├── security/
│   │   ├── JwtProvider.java
│   │   └── JwtAuthenticationFilter.java
│   └── exception/
│       ├── ErrorResponse.java
│       ├── GlobalExceptionHandler.java
│       └── OptimisticLockException.java
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/
│       └── V1__init_schema.sql
├── pom.xml
└── Dockerfile
```

## Configuración

```properties
# application.properties
server.port=8083
spring.datasource.url=jdbc:postgresql://localhost:5433/db_seats
spring.datasource.username=postgres
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=true

# API Gateway URL (para comunicarse con otros servicios)
api.gateway.url=http://localhost:3000
```
