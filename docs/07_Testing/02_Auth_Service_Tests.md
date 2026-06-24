# Pruebas de Integración: Auth Service

Este documento detalla los casos de prueba de integración (Caja Negra) implementados para el servicio de autenticación. Estas pruebas utilizan `MockMvc` para simular peticiones HTTP y `Testcontainers` (PostgreSQL) para verificar la persistencia de datos reales.

## 1. Endpoint: POST /api/auth/register

| Caso de Prueba | Payload (JSON) | Resultado Esperado | Código HTTP | Verificación en BD |
| :--- | :--- | :--- | :--- | :--- |
| **Registro Exitoso** | `{ "email": "test@cinestar.com", "contrasena": "Secure123*", "nombres": "Juan", "apellidos": "Perez" }` | Retorna JWT token en la respuesta. | `201 CREATED` | El usuario debe existir en la tabla `usuarios` y su contraseña debe estar encriptada con BCrypt. |
| **Email Duplicado** | El mismo payload anterior (enviado por segunda vez). | Mensaje de error indicando que el email ya está registrado. | `400 BAD REQUEST` | No debe insertarse un nuevo registro. |
| **Validaciones: Formato Email Inválido** | `{ "email": "correo-sin-arroba", "contrasena": "Secure123*", "nombres": "A", "apellidos": "B" }` | Lista de errores de validación (`MethodArgumentNotValidException`). | `400 BAD REQUEST` | No interactúa con la BD. |
| **Validaciones: Contraseña Corta** | `{ "email": "valid@cinestar.com", "contrasena": "123", "nombres": "A", "apellidos": "B" }` | Error de validación: "La contraseña debe tener al menos 6 caracteres". | `400 BAD REQUEST` | No interactúa con la BD. |
| **Validaciones: Campos Vacíos** | `{}` | Errores múltiples por `@NotBlank`. | `400 BAD REQUEST` | No interactúa con la BD. |

## 2. Endpoint: POST /api/auth/login

| Caso de Prueba | Payload (JSON) | Resultado Esperado | Código HTTP |
| :--- | :--- | :--- | :--- |
| **Login Exitoso** | `{ "email": "test@cinestar.com", "contrasena": "Secure123*" }` | Retorna JWT válido generado por la aplicación. | `200 OK` |
| **Contraseña Incorrecta** | `{ "email": "test@cinestar.com", "contrasena": "WrongPass!" }` | Error indicando credenciales inválidas. | `401 UNAUTHORIZED` |
| **Usuario No Existe** | `{ "email": "noexiste@cinestar.com", "contrasena": "Secure123*" }` | Error indicando credenciales inválidas (para no dar pistas a atacantes). | `401 UNAUTHORIZED` |
