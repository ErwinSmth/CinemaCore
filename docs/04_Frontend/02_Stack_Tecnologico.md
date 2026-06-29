# Stack Tecnológico del Frontend

Este documento define las tecnologías elegidas para el desarrollo del Frontend de CinemaCore. La selección se basa en maximizar el rendimiento (velocidad de carga), favorecer el SEO para el catálogo público de películas y asegurar una alta mantenibilidad y tipado estricto al comunicarse con la arquitectura de microservicios.

## 1. Tecnologías Core

*   **Framework Base:** `Next.js` (App Router)
    *   **Propósito:** Framework de React que permite Server-Side Rendering (SSR) y Static Site Generation (SSG). Es crucial para garantizar que el catálogo de películas sea indexado por los motores de búsqueda (SEO) y ofrezca tiempos de primera pintura ultrarrápidos.
*   **Lenguaje:** `TypeScript`
    *   **Propósito:** Provee tipado estático, previniendo errores en tiempo de ejecución. Los tipos se mapearán exactamente a los DTOs y respuestas que emite el API Gateway.

## 2. Estilos y Diseño (UI)

*   **Estilización:** `Tailwind CSS v4`
    *   **Propósito:** Framework de CSS basado en clases utilitarias. Permite iteración rápida y se acopla perfectamente con herramientas de generación visual por IA (como v0.dev).
*   **Componentes Base:** `shadcn/ui`
    *   **Propósito:** Provee componentes accesibles y hermosos (modales, botones, menús) que no vienen empaquetados en una librería pesada (como Bootstrap o Material UI), sino que el código fuente se copia al proyecto, otorgando 100% de control sobre el diseño.
*   **Filosofía de Diseño:**
    *   **Balance:** Se busca una interfaz simple e intuitiva, pero que atrape la atención visualmente (mediante micro-animaciones, manejo de sombras profundas y contrastes que transmitan la "experiencia de cine").

## 3. Consumo de Datos y Estado

*   **Data Fetching & Caché:** `TanStack Query` (React Query)
    *   **Propósito:** Maneja de forma asíncrona todas las peticiones hacia el **API Gateway**. Gestiona automáticamente los estados de carga (`isLoading`), los errores (`isError`), re-intentos y mantiene un caché inteligente en el navegador del cliente para evitar peticiones redundantes.
*   **Estado Global:** `Zustand`
    *   **Propósito:** Gestor de estado ultra ligero. Se utilizará exclusivamente para el estado global síncrono que no depende del servidor (por ejemplo: asientos seleccionados temporalmente en el flujo de compra, preferencias de tema oscuro/claro, alertas globales).

