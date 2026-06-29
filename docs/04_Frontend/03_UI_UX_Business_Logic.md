# Lógica de Negocio UI/UX - Frontend

Este documento detalla las reglas de negocio y decisiones de diseño para los componentes visuales de la interfaz, asegurando consistencia a medida que el proyecto crece.

## 1. Carrusel Principal (Hero Banner)
- **Películas mostradas**: Títulos destacados (tanto de cartelera como próximos estrenos con preventa).
- **Indicador de Estado**: 
  - Si es estreno futuro: Etiqueta "Próximamente".
  - Si es preventa activa: El botón principal debe decir "COMPRAR PREVENTA".
  - Si está en cartelera: El botón principal dice "COMPRAR ENTRADAS".

## 2. Sección "Próximos Estrenos" (Pre-Estrenos)
Las películas en estado `PRE-ESTRENO` se muestran aquí. Existen sub-estados importantes:
- **Película Futura (Sin Preventa)**: Solo muestra fecha de estreno y opción "Ver Tráiler".
- **Película Futura (Con Preventa Activa)**: 
  - Aparece un badge (etiqueta) rojo parpadeante en la tarjeta indicando "PREVENTA ACTIVA".
  - Al pasar el ratón (hover) sobre la tarjeta, se deben mostrar **dos botones**:
    1. `Comprar Preventa` (Rojo destacado).
    2. `Ver Tráiler` (Secundario/Glassmorphism).

*Nota técnica*: Esta lógica dependerá de un flag como `hasActivePresale: true` enviado por el backend (Microservicio de Funciones/Cartelera) en el futuro.

## 3. Tráilers Destacados (Featured Trailers)
- **Data Source**: Se debe combinar la lista de `Próximos Estrenos` y `Cartelera`, filtrando estrictamente las películas que contengan al menos un tráiler en su array `trailers`.
- **Prioridad/Orden**: Se debe priorizar (mostrar primero) los tráilers de los `Próximos Estrenos`, ya que generan mayor expectativa en los usuarios, seguidos por los de la cartelera actual.
- **Rendimiento**: En lugar de usar las imágenes de alta resolución de TMDB (`backdrop_path`), se extraen dinámicamente las **miniaturas nativas de YouTube** (`mqdefault.jpg`). Esto reduce el peso por imagen de ~200KB a ~15KB, manteniendo la carga de la página instantánea.

## 4. Compra Rápida (Sticky Footer)
- **Propósito**: Permitir al usuario iniciar un flujo de compra desde cualquier parte de la vista principal sin tener que navegar hacia arriba.
- **Flujo**:
  1. El usuario hace clic en "Película" o "Fecha".
  2. Se despliega un menú flotante sobre la barra.
  3. Tras seleccionar una película, se habilitan las fechas disponibles.
  4. Tras seleccionar una fecha, se habilitan las horas (funciones) disponibles.
- **Integración**: Este componente se nutrirá directamente del `showtime-service` (Microservicio de Funciones) cuando esté desarrollado.
