---
description: Documentación de la Infraestructura base (Docker)
related_skills:
  - docker
---

# Infraestructura y Contenedores (Docker)

El proyecto **CinemaCore** utiliza un enfoque de contenedores vía **Docker Compose** para orquestar las piezas fundamentales de infraestructura compartida (Bases de Datos, Caché, Message Brokers) requeridas por los microservicios en sus ambientes locales de desarrollo.

> [!NOTE]
> Esta infraestructura es **agnóstica** a los lenguajes de los microservicios (Java, Node.js). Los microservicios en desarrollo (ej. Quarkus, Express, Spring Boot) se conectan a estos contenedores mapeados a `localhost`.

---

## 1. Topología del `docker-compose.yml`

El archivo central de orquestación se encuentra en `cinestar-infra/docker-compose.yml`. 

### 1.1. PostgreSQL (Base de Datos Centralizada)
Aunque la arquitectura es de microservicios (Database-per-service), a nivel de contenedor **local** utilizamos una única instancia de PostgreSQL alojando múltiples bases de datos lógicas (`db_auth`, `db_movies`, etc.)

*   **Imagen:** `postgres:15`
*   **Contenedor:** `cinestar-postgres`
*   **Puerto Mapeado:** `5433:5432` (Se mapea al 5433 en la máquina Host para evitar colisiones con instancias locales preexistentes de Postgres).
*   **Credenciales:**
    *   Usuario: `postgres`
    *   Contraseña: `root`
*   **Persistencia:** Volumen de Docker `postgres_data` mapeado a `/var/lib/postgresql/data`.
*   **Inicialización:** Al iniciar un volumen virgen, ejecuta los scripts ubicados en `cinestar-infra/init-db/init.sql` para crear las bases de datos vacías para cada microservicio. Las **estructuras de tablas** se crean de forma descentralizada por Flyway en cada microservicio.

### 1.2. Redis (Caché Centralizado)
Utilizado para mitigar la sobrecarga de consultas estáticas o semánticas (ej. la cartelera pública de películas) y optimizar los tiempos de respuesta.

*   **Imagen:** `redis:7-alpine` (Versión aligerada basada en Alpine Linux).
*   **Contenedor:** `cinestar-redis`
*   **Puerto Mapeado:** `6379:6379`
*   **Comportamiento (Warming & Cache-Aside):** 
    *   Configurado con `--save 60 1` y `--loglevel warning`.
    *   Los microservicios como el *Movie Service* (Quarkus) almacenarán allí los JSON completos de las respuestas de TMDB o de su propia API pública para entregar respuestas en latencia menor a 10ms.

---

## 2. Flujo de Trabajo del Desarrollador (Developer Workflow)

Para iniciar a programar en cualquier microservicio del monorepo, el requisito previo indispensable es levantar la infraestructura base:

```bash
# 1. Navegar a la carpeta de infraestructura
cd cinestar-infra

# 2. Levantar los servicios en segundo plano (detached mode)
docker-compose up -d

# 3. Verificar que los contenedores estén sanos (Up)
docker-compose ps
```

Una vez ejecutados los contenedores, las aplicaciones (API Gateway en puerto 8080, Movie Service en 8082, etc.) se comunican con `localhost:5433` (Postgres) y `localhost:6379` (Redis).
