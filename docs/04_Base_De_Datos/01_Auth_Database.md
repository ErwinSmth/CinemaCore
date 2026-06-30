---
description: Esquema de Base de Datos para Auth Service
related_skills:
  - postgresql
  - spring-boot
---

# Data Model: Auth Service (`db_auth`)

## Diagrama Relacional (Entidad-Relación)

La base de datos `db_auth` utiliza una relación de **Muchos a Muchos (N:M)** clásica entre Usuarios y Roles, facilitando que un usuario pueda tener múltiples roles (ej. un Taquillero que también tiene cuenta de Cliente).

### 1. Tabla `usuario` (Users)
Almacena las credenciales principales y datos de identidad.
*   `user_id` (BIGSERIAL / BIGINT, Primary Key)
*   `email` (VARCHAR 255, Unique, Not Null)
*   `contraseña` (VARCHAR 255, Not Null): Almacena el hash generado por BCrypt, nunca el texto plano.
*   `nombres` (VARCHAR 100)
*   `apellidos` (VARCHAR 100)
*   `estado` (VARCHAR 20 / BOOLEAN): Define si la cuenta está activa, bloqueada o inactiva.
*   `fecha_creacion` (TIMESTAMP)

### 2. Tabla `roles` (Roles)
Diccionario estático de roles de seguridad.
*   `rol_id` (SERIAL / INTEGER, Primary Key)
*   `nombre` (VARCHAR 50, Unique, Not Null): Ejemplos: `ROLE_CLIENTE`, `ROLE_ADMINISTRADOR`, `ROLE_TAQUILLERO`.

### 3. Tabla `user_role` (Junction Table)
Tabla intermedia que resuelve la relación Muchos a Muchos.
*   `user_id` (BIGINT, Foreign Key -> `usuario.user_id`)
*   `rol_id` (INTEGER, Foreign Key -> `roles.rol_id`)
*   *(Opcional pero recomendado)*: Primary Key compuesta por `(user_id, rol_id)` para evitar que a un usuario se le asigne el mismo rol dos veces.

---

## Notas de Implementación SDD
*   **Contrato JWT:** Al momento de generar el JWT (Login), el backend hará un `JOIN` con la tabla `roles` para inyectar el arreglo de roles (ej. `["ROLE_CLIENTE", "ROLE_TAQUILLERO"]`) dentro del payload del token.
