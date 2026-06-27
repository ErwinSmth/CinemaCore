---
description: Documentación de la Infraestructura y Despliegue (Docker)
related_skills:
  - docker
---

# Infraestructura y Contenedores (Docker)

El proyecto **CinemaCore** utiliza un enfoque de orquestación **Full-Stack** vía **Docker Compose**. A diferencia de un entorno donde los servicios se levantan manualmente, nuestro `docker-compose.yml` ubicado en la **raíz del proyecto** (`/`) permite desplegar toda la aplicación (Bases de Datos, Caché, Frontend, Gateway y Microservicios) con un solo comando.

Esta arquitectura está diseñada para ser un entorno de "producción-clonable", ideal para portafolios y para evitar el clásico "en mi máquina sí funciona".

---

## 1. Topología del `docker-compose.yml` (Root)

Todos los contenedores están conectados a una red interna llamada `cinestar-network`, lo que permite que se comuniquen entre sí mediante sus nombres de servicio (ej: `http://cinestar-movie-service:8082`) en lugar de `localhost`.

### 1.1. Infraestructura Base
*   **PostgreSQL (`cinestar-postgres`)**:
    *   **Imagen:** `postgres:15-alpine` (Evitar la etiqueta `latest` por estabilidad).
    *   **Puerto:** `5433:5432` (Mapeado al 5433 externo para evitar colisiones).
    *   **Inicialización:** Ejecuta el script montado desde `.docker/postgres/init-db/init.sql` para crear las bases de datos lógicas (`db_auth`, `db_movies`, etc.).
*   **Redis (`cinestar-redis`)**:
    *   **Imagen:** `redis:7-alpine`
    *   **Puerto:** `6379:6379`
    *   Utilizado para la caché de respuestas rápidas de la cartelera.

### 1.2. Microservicios (Multi-stage Alpine)
Cada microservicio cuenta con un `Dockerfile` optimizado con patrón **Multi-stage build**:
*   **Optimizaciones:** Compilación aislada con BuildKit (`--mount=type=cache`), ejecución con usuario `no-root`, e imágenes base ultra-ligeras (`eclipse-temurin:17-jre-alpine` o `node:20-alpine`).
*   **Compatibilidad Windows/WSL2:** Los servicios Java están tuneados estrictamente en memoria (`-Xms64m -Xmx128m` y `SerialGC`) para no saturar los hosts de los desarrolladores.
*   **Servicios:**
    *   `cinestar-api-gateway` (Node.js) -> Puerto `8080`
    *   `cinestar-auth-service` (Spring Boot) -> Puerto `8081`
    *   `cinestar-movie-service` (Spring Boot) -> Puerto `8082`

### 1.3. Monitoreo de Logs (Dozzle)
Para facilitar la depuración, especialmente en entornos mixtos (Windows/Linux), se incluye **Dozzle**.
*   **Imagen:** `amir20/dozzle:v8`
*   **Puerto:** `9999:8080`
*   Permite a los desarrolladores entrar a `http://localhost:9999` en su navegador para buscar, filtrar y visualizar los logs de cualquier contenedor en tiempo real, sin usar la terminal.

---

## 2. Flujo de Trabajo del Desarrollador (Developer Workflow)

Para levantar TODO el ecosistema de Cinestar, asegúrate de tener tu archivo `.env` en la raíz (creado a partir de `.env.example`) y ejecuta:

```bash
# 1. En la raíz del proyecto, construir y levantar todo en segundo plano
docker compose up -d --build

# 2. Ver los logs en vivo desde el navegador
# -> Abrir http://localhost:9999

# 3. Detener todo y limpiar la red
docker compose down
```
