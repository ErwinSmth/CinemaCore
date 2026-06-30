# Directrices de Frontend

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
   - *Nota:* La respuesta de TMDB es un objeto JSON paginado (`{ results: [...] }`), por lo que el servicio Angular **debe** usar `map` de RxJS para extraer el arreglo y evitar fallas silenciosas en la vista.

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

## 3. UI/UX y Reglas Críticas de Desarrollo Angular

Para mantener la calidad y prevenir bugs persistentes, todo desarrollo en Angular debe adherirse a lo siguiente:

### 3.1. Abismo Idiomático (Data Mapping Estricto)
- **El Problema:** El frontend utiliza nombres de variables en español para comodidad visual (`titulo`, `sinopsis`, `estado`, `restriccionCodigo`), mientras que el backend exige contratos OpenAPI estrictos en inglés (`title`, `overview`, `status`, `ageRating`).
- **La Regla:** **Ningún componente debe enviar su formulario directamente al HttpClient.** El servicio (`movie.service.ts`) DEBE interceptar y mapear explícitamente las propiedades del español al formato JSON en inglés que espera el servidor. Enviar nulos al backend por fallos de nombres ocasiona actualizaciones silenciosas fallidas.

### 3.2. Estética y Modales (Glassmorphism)
- El diseño utiliza el estilo oscuro "Glassmorphism" (ej. fondos `bg-[#0a1120]`, `bg-black/80`, bordes sutiles `border-slate-800`).
- **Prohibición:** Está estrictamente **prohibido** el uso de ventanas nativas del navegador como `alert()` o `confirm()`. Todas las interacciones destructivas (como eliminar) o informativas deben usar Modales HTML/Tailwind personalizados con animaciones e iconos consistentes.

### 3.3. Reactividad y Manejo de Caché
- **Anti-Patrón Prohibido:** No usar `setTimeout` para esperar que el backend termine de guardar o importar datos. Esto genera condiciones de carrera (race conditions), especialmente con redes lentas.
- **La Regla:** Usar encadenamiento de observables (RxJS). Después de hacer un POST/PUT/DELETE, se debe ejecutar un `.subscribe()` que llame a las funciones de recarga (ej. `loadMovies()`).
- **Sincronización de Caché:** Dado que el backend utiliza Redis para cachear consultas públicas, cualquier operación del Admin (Importar, Editar, Eliminar) **debe** disparar silenciosamente peticiones para refrescar el estado global del frontend (ej. llamar a `fetchCartelera()` tras guardar una edición), asegurando que el administrador vea los cambios aplicados inmediatamente.
