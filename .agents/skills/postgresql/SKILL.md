---
name: postgresql
description: Guía de PostgreSQL 15, esquemas, migraciones, optimización
---

# PostgreSQL Skill - Cinestar Backend

## Configuración

- **Versión**: PostgreSQL 15
- **Puerto**: 5433 (host) → 5432 (container)
- **Usuario**: postgres / root
- **4 Bases de datos**: db_auth, db_movies, db_showtime, db_seats

## Esquemas por Servicio

### db_auth (Auth Service)
```sql
CREATE TABLE roles(
  rol_id SERIAL PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuarios(
  user_id BIGSERIAL PRIMARY KEY,
  email VARCHAR(100) NOT NULL UNIQUE,
  contrasena VARCHAR(255) NOT NULL,
  nombres VARCHAR(100) NOT NULL,
  apellidos VARCHAR(100) NOT NULL,
  estado BOOLEAN DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_role(
  user_id BIGINT NOT NULL,
  rol_id INT NOT NULL,
  PRIMARY KEY (user_id, rol_id),
  FOREIGN KEY (user_id) REFERENCES usuarios(user_id) ON DELETE CASCADE,
  FOREIGN KEY (rol_id) REFERENCES roles(rol_id) ON DELETE CASCADE
);

INSERT INTO roles (nombre) VALUES ('ROLE_CLIENTE'), ('ROLE_ADMINISTRADOR'), ('ROLE_TAQUILLERO');
```

### db_movies (Movie Service)
```sql
-- Pendiente de diseño
```

### db_showtime (Showtime Service)
```sql
-- Pendiente de diseño
```

### db_seats (Seat Service)
```sql
-- Pendiente de diseño (usuario proporcionará esquema)
```

## Migraciones Flyway

- **Nombre**: `V{version}__{description}.sql`
- **Ejemplo**: `V1__init_schema.sql`, `V2__add_index.sql`
- **Code-First**: Escribir SQL manualmente, no usar `ddl-auto`
- ** Orden**: Flyway ejecuta en orden numérico

## Patrones Comunes

### Tabla con Soft Delete
```sql
CREATE TABLE ejemplo(
  id BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  activo BOOLEAN DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Índices
```sql
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_tickets_funcion ON tickets(funcion_id);
CREATE INDEX idx_tickets_asiento ON tickets(asiento_id);
```

### Optimistic Locking (para Seat Service)
```sql
CREATE TABLE tickets(
  ticket_id BIGSERIAL PRIMARY KEY,
  funcion_id BIGINT NOT NULL,
  asiento_id BIGINT NOT NULL,
  usuario_id BIGINT,
  status VARCHAR(20) DEFAULT 'AVAILABLE',
  locked_until TIMESTAMP,
  version INT DEFAULT 0
);
```

## Comandos Útiles

```bash
# Conectar a una base de datos
psql -h localhost -p 5433 -U postgres -d db_auth

# Listar bases de datos
psql -h localhost -p 5433 -U postgres -l

# Ver tablas de una DB
psql -h localhost -p 5433 -U postgres -d db_auth -c "\dt"

# Ver estructura de tabla
psql -h localhost -p 5433 -U postgres -d db_auth -c "\d usuarios"
```
