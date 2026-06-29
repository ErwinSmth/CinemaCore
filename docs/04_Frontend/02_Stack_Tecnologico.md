# Stack Tecnológico del Frontend

Este documento define las tecnologías elegidas para el desarrollo del Frontend de CinemaCore. La selección se basa en maximizar el rendimiento (velocidad de carga), favorecer el SEO para el catálogo público de películas y asegurar una alta mantenibilidad y tipado estricto al comunicarse con la arquitectura de microservicios.

## 1. Tecnologías Core

*   **Framework Base:** `Angular (Standalone Components)`
    *   **Propósito:** Framework robusto de nivel empresarial que incluye enrutamiento, cliente HTTP y manejo de reactividad avanzado por defecto. Su arquitectura basada en Inyección de Dependencias y Servicios garantiza un código altamente escalable para el flujo de cine.
*   **Lenguaje:** `TypeScript`
    *   **Propósito:** Provee tipado estático estricto, previniendo errores en tiempo de ejecución. Los tipos se mapearán exactamente a los DTOs y respuestas que emite el API Gateway.

## 2. Estilos y Diseño (UI) - Design Tokens Oficiales

Tras validar el prototipo visual (v0), se ha definido la siguiente guía de estilos **utilitaria, plana y oscura** (inspirada en cadenas internacionales), evitando adornos excesivos.

*   **Estilización Base:** `Tailwind CSS v4`
*   **Componentes Base:** `spartan/ui` (clon directo de shadcn/ui para Angular, con estilo flat, sin sombras gruesas).

### Paleta de Colores (Tailwind Classes)
*   **Fondo Principal (Background):** `bg-slate-900` (Gris oscuro elegante, no negro puro).
*   **Superficies y Tarjetas (Cards/Modals):** `bg-slate-800`.
*   **Elementos Interactivos (Inputs/Bordes):** Fondo `bg-slate-700` con borde `border-slate-600`.
*   **Color Primario (Acento):** `bg-red-600` (hover: `bg-red-700`) para botones principales ("Get Tickets", "Sign In").
*   **Textos:** `text-white` para títulos principales, `text-slate-300` para descripciones y `text-slate-400` para texto secundario.

### Estilo Estructural
*   **Minimalismo:** Las películas en la cartelera no llevan contenedores pesados (cards) ni sombras, consisten únicamente en el Póster, Título, Rating (`text-yellow-500`) y el botón rojo de compra.
*   **Bordes:** Ligeramente redondeados (`rounded`), evitando radios exagerados.

## 3. Consumo de Datos y Estado

*   **Data Fetching:** `Angular HttpClient` + `RxJS`
    *   **Propósito:** Maneja de forma asíncrona todas las peticiones hacia el **API Gateway**. Mediante interceptores (HttpInterceptor) inyectaremos el token JWT globalmente. Los observables de RxJS nos permitirán cancelar peticiones (ej. debounce en búsquedas) y manejar flujos de datos complejos.
*   **Estado Global (Local):** `Angular Signals` (Servicios Singletone)
    *   **Propósito:** Gestor de estado reactivo y ultraligero nativo de Angular. Se utilizará para el estado global síncrono (ej. almacenar el JWT, rol de usuario en `AuthService`, o los asientos seleccionados temporalmente) evitando librerías pesadas externas.
