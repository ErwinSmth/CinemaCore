0# Directrices de Frontend

Este documento recopila decisiones arquitectónicas y flujos de experiencia de usuario (UX) cruciales para el desarrollo del Frontend de CinemaCore. El objetivo es mantener una guía de referencia que evite errores de diseño y acoplamiento incorrecto con los microservicios.

## 1. Integración con TMDB (Gestión de Películas)

El módulo de administración encargado de registrar (importar) nuevas películas desde la base de datos externa de TMDB (The Movie Database) presenta un riesgo de calidad de datos, dado que TMDB permite registros incompletos o vacíos creados por su comunidad.

Para mitigar esto, el flujo en el Frontend debe seguir estrictamente este modelo de **2 Pasos Visuales** y evitar a toda costa realizar importaciones "ciegas".

### Flujo de Importación Recomendado

1. **Búsqueda Visual y Presentación en Grilla:**
   - El administrador ingresa un término de búsqueda (ej. `avengers: endgame`).
   - El Frontend llama al endpoint del API Gateway: `GET /api/v1/movies/tmdb/search?query=avengers:%20endgame`
   - El Frontend **DEBE** renderizar la respuesta como una lista visual (grilla o lista de tarjetas). Cada tarjeta debe mostrar al menos:
     - Título original.
     - Póster (miniatura).
     - Fecha de lanzamiento o año.
   - *Nota:* Esto permite al administrador notar visualmente resultados "basura" (que usualmente carecen de póster o tienen años incorrectos).

2. **Selección Manual (Click to Import):**
   - Cada tarjeta debe tener un botón de acción explícito (ej. "Importar al Catálogo").
   - Sólo cuando el administrador hace clic en ese botón, el Frontend llama al endpoint de importación con el ID específico de TMDB seleccionado: `POST /api/v1/movies/tmdb/import/{tmdbId}`.

### Validaciones en el Cliente

- El Frontend debe estar preparado para recibir y manejar de forma limpia un error HTTP `400 Bad Request` proveniente del proceso de importación.
- El Backend implementa un **Quality Gate**. Si la película que se intenta importar no tiene información básica (sinopsis, póster, géneros, etc.), el backend la rechazará.
- Al recibir el error `400`, el Frontend mostrará una alerta (Toast/Snackbar) indicando: *"La película seleccionada no cuenta con información suficiente en TMDB. Elija otra versión o resultado válido."*

## 2. API Gateway como Único Punto de Entrada

Por norma de seguridad de la arquitectura (Zero Trust):
- El Frontend **JAMÁS** debe apuntar directamente a los microservicios individuales (puertos 8081, 8082, etc.).
- Absolutamente todas las peticiones (ya sean públicas para el cliente o privadas para el administrador) deben canalizarse a través del **API Gateway** (`http://localhost:8080`).
- Es responsabilidad del API Gateway inyectar los headers de seguridad (`X-User-Id`, `X-User-Role`) necesarios para la autorización en los microservicios tras validar el JWT.
