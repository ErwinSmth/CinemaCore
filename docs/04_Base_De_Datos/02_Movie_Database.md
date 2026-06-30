---
description: Esquema de Base de Datos para Movie Service
related_skills:
  - postgresql
  - spring boot
---

# Data Model: Movie Service (`db_movies`)

## Diagrama Relacional (Entidad-Relación)

La base de datos `db_movies` utiliza un enfoque híbrido que mezcla el modelo relacional tradicional para búsquedas de alta velocidad, con el almacenamiento de documentos (JSONB) para flexibilidad y rendimiento en lectura de metadata no estructurada proveniente de TMDB.

### 1. Tabla `restriccion_edad` (Age Restrictions)
Tabla de catálogo o diccionario que define las clasificaciones por edad permitidas.
*   `restriccion_id` (INTEGER / UUID, Primary Key)
*   `codigo` (VARCHAR 10, Unique): "APT", "7+", "12+", "14+", "16+", "18+". **Advertencia Crítica:** El símbolo "+" va estrictamente al final. Si el Frontend envía "+18" en lugar de "18+", la base de datos lo ignorará y fallará silenciosamente.
*   `descripcion` (VARCHAR 100): Ej. "Apto para todos", "Mayores de 14 años".

### 2. Tabla `peliculas` (Movies)
Tabla principal del catálogo de cine.
*   `peliculas_id` (UUID, Primary Key)
*   `tmdb_id` (INTEGER, Unique): Identificador de la API global (The Movie Database).
*   `titulo` (VARCHAR 255)
*   `sinopsis` (TEXT)
*   `duracion_min` (INTEGER): Vital para el cálculo de horarios en el Showtime Service.
*   `fecha_estreno` (DATE)
*   `estado` (VARCHAR 20): Por defecto `INACTIVO` para ocultar las importaciones de TMDB hasta su aprobación. Diferencia clave de estados finales:
    *   **RETIRADA:** Película que terminó su ciclo en cines exitosamente. Se mantiene para historial de ventas y contabilidad (Soft Delete del ciclo normal).
    *   **ELIMINADA:** Película importada por accidente o error humano que nunca debió estar. Se oculta pero no se hace hard-delete por integridad referencial (Soft Delete de error).
*   `restriccion_id` (INTEGER / UUID, Foreign Key -> `restriccion_edad.restriccion_id`): Relación N:1 con el catálogo de edades.
*   `metadata` (JSONB): Columna especial para almacenar datos anidados o variables como listas de actores, directores, URLs de imágenes y URLs de trailers de YouTube.

---

## Notas de Implementación (Optimización y SDD)
*   **Velocidad de Lectura:** El Frontend solicita la cartelera filtrando por `estado = 'CARTELERA'`. La base de datos devuelve las columnas estándar más el bloque `metadata` completo sin necesidad de hacer múltiples `JOINs` pesados a tablas de "actores" o "trailers".
*   **JSONB de PostgreSQL:** Se debe garantizar que la entidad de JPA (Hibernate en Spring Boot) esté configurada con tipos de datos que soporten el mapeo nativo hacia `JSONB`.
