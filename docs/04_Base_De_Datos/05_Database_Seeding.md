# Estrategia de Database Seeding (A Implementar)

Para poder realizar pruebas tanto manuales (vía Bruno) como automatizadas en entornos de desarrollo local, requerimos que la base de datos central tenga información precargada (Seed Data).

## Consideraciones

1. **Datos de Prueba Iniciales:**
   - Usuarios base (Ej: `admin@cinestar.com`, `cliente@cinestar.com`).
   - Películas de ejemplo con su estado en `CARTELERA`.
   - Salas (Ej: Sala 1, Sala 2).
   - Funciones programadas y sus asientos base.

2. **Mecanismo de Inserción (Pendiente de decisión final):**
   - **Opción A (Flyway):** Incluir un script `V999__insert_seed_data.sql` en las migraciones de Spring Boot / Quarkus, restringido solo al perfil de desarrollo (`dev`).
   - **Opción B (Código - Runners):** Usar `CommandLineRunner` en Spring Boot o `@Observes StartupEvent` en Quarkus, leyendo archivos JSON en `src/main/resources` e insertando en base de datos si las tablas están vacías.

> **Estado:** Pendiente de implementación. Se priorizará cuando se integren los servicios de Películas y Horarios para tener flujos complejos que probar.
