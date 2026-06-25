---
name: docs-planner
description: Reglas y flujos de trabajo para la planificación, análisis de negocio y desarrollo de documentación antes de codificar.
---

# Docs Planner Skill - Arquitectura y Negocio Primero

Esta habilidad define el flujo estricto que un agente debe seguir al planificar y documentar un nuevo microservicio o componente en CinemaCore, evitando el "salto prematuro" a la tecnología.

## 🛑 Regla de Oro
**NUNCA** propongas código, comandos de compilación, ni discutas detalles técnicos profundos (Redis, SQL, Índices, JSONB) si el flujo de negocio puro (Casos de Uso) no está 100% definido y aprobado por el usuario.

## 📋 Flujo de Documentación (Fases Obligatorias)

Cuando se te pida "documentar o planificar un servicio", debes seguir estas fases secuencialmente con el usuario:

### Fase 1: Análisis de Negocio (El "Qué")
1. **Casos de Uso:** Identifica qué acciones hará el Administrador y qué hará el Cliente.
2. **Diagrama de Negocio:** Crea un diagrama de flujo en Mermaid (`flowchart TD`) puro, sin mencionar bases de datos o cachés. Solo actores y acciones (Ej. `Admin --> Buscar Película`).
3. **Discusión:** Propón activamente casos de borde o requerimientos faltantes (Ej. "¿Qué pasa si se elimina por error?"). No avances a la Fase 2 hasta que el usuario apruebe los CUs.

### Fase 2: Diseño Técnico y API Contract (El "Cómo")
1. **Mapeo REST:** Por cada Caso de Uso aprobado, diseña su endpoint correspondiente (`GET`, `POST`, `PUT`, `DELETE`).
2. **Seguridad:** Define si el endpoint es Público o Privado (requiere JWT y Roles específicos).
3. **Estrategias:** Plasma las estrategias de persistencia (Soft Delete, Bloqueo Optimista) y almacenamiento (Caché Redis, PostgreSQL JSONB).
4. **Dependencias:** Define si el servicio consume APIs externas (ej. TMDB) o si otros servicios dependen de él.

### Fase 3: Aceptación y Pruebas (El "Cuándo está listo")
1. **Criterios de Aceptación (TDD):** Redacta escenarios en formato `Given / When / Then` para guiar el desarrollo futuro.

## 📝 Reglas de Formato (Markdown)
*   **Estructura:** Todo servicio debe tener las siguientes secciones obligatorias:
    1. Responsabilidad
    2. Stack Tecnológico
    3. Dependencias Inter-servicio
    4. Configuración (Env Vars)
    5. Casos de Uso (Con Mermaid)
    6. Estrategia de Persistencia / Caché
    7. Manejo de Errores
    8. API Contract (Endpoints)
    9. Acceptance Criteria (TDD)
*   **Mermaid:** Para diagramas de flujo, usa la sintaxis compatible con Obsidian. Evita `rect rgb()` y anida etiquetas en subgraphs usando `subgraph ID [Título]`. Las flechas de texto deben ser `-.->|Texto|`.
*   **Callouts:** Usa GitHub Alerts para resaltar información importante:
    `> [!IMPORTANT]` para aprobaciones.
    `> [!WARNING]` para puntos ciegos o consideraciones de seguridad.
