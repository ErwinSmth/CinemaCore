---
description: Esquema de Base de Datos para Showtime Service (Programación de Funciones)
related_skills:
  - postgresql
  - quarkus
---

# Data Model: Showtime Service (`db_showtime`)

## Diagrama Relacional (Entidad-Relación)

La base de datos `db_showtime` organiza la cartelera operativa, mapeando qué película se proyecta, en qué sala, a qué hora y en qué formato.

### 1. Tabla `salas` (Rooms/Theaters)
Representa los espacios físicos del cine.
*   `sala_id` (UUID, Primary Key)
*   `nombre` (VARCHAR 100): Ej. "Sala 1 VIP".
*   `capacidad` (INTEGER): Capacidad total de butacas.
*   `estado` (VARCHAR 20): Ej. "ACTIVA", "MANTENIMIENTO".

### 2. Tabla `proyeccion` (Projection Formats)
Catálogo estático de tipos de proyección.
*   `proyeccion_id` (INTEGER / UUID, Primary Key)
*   `codigo` (VARCHAR 10, Unique): Ej. "2D", "3D", "IMAX".
*   `descripcion` (VARCHAR 100): Ej. "Proyección Digital 3D".

### 3. Tabla `funcion` (Showtimes)
La tabla transaccional principal que une películas con salas y tiempos.
*   `funcion_id` (UUID, Primary Key)
*   `movie_id` (UUID): **Referencia Externa** al `Movie Service` (`db_movies.peliculas_id`). No hay foreign key nativa de base de datos directa por estar en microservicios separados.
*   `sala_id` (UUID, Foreign Key -> `salas.sala_id`)
*   `proyeccion_id` (INTEGER / UUID, Foreign Key -> `proyeccion.proyeccion_id`)
*   `fecha_inicio` (TIMESTAMP): Hora exacta de inicio.
*   `fecha_fin` (TIMESTAMP): Calculada sumando la duración de la película + limpieza.
*   `precio_ticket` (DECIMAL / NUMERIC): Precio base para esta función.
*   `status` (VARCHAR 20): Ej. "PROGRAMADA", "EN_CURSO", "FINALIZADA", "CANCELADA".

---

## Notas de Arquitectura (Integración de Datos)
*   Dado el patrón de microservicios, el `Showtime Service` solo almacena el `movie_id`. Cuando el Frontend necesita pintar la cartelera, solicitará a este servicio los horarios y luego al `Movie Service` la metadata (pósters, títulos), o el API Gateway orquestará ambas llamadas.
*   **Evento Crítico:** Al crearse un registro exitoso en la tabla `funcion`, este servicio debe comunicarse con el **Seat Service** (vía llamada síncrona HTTP) para disparar la **pre-generación de tickets** asociados a esta `funcion_id` y a la capacidad de la `sala_id`.
