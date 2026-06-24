# API Gateway Tests

## 1. Estrategia General
El API Gateway se prueba a nivel de integración utilizando `Supertest` para levantar la aplicación Express en memoria. Para mantener el aislamiento entre microservicios, **no** dependemos del Auth Service real. En su lugar, interceptamos las llamadas a los clientes HTTP (Axios) usando `jest.mock()`.

**Objetivos principales:**
1. Validar que el middleware de `express-openapi-validator` rechace payloads mal formados según el `api-spec.yml`.
2. Validar que las rutas del API Gateway invoquen correctamente a sus controladores y deleguen al cliente.
3. Asegurar que el manejo de errores global centralizado formatea las respuestas correctamente.

## 2. Casos de Prueba (Auth Routes)

Archivo: `backend/api-gateway/src/routes/auth.routes.test.ts`

### Escenario 1: Validación de Esquema OpenAPI (Login)
- **Acción:** `POST /api/v1/auth/login` con payload `{ email: "test@correo.com" }` (Falta `contrasena`).
- **Resultado Esperado:** 
  - Status Code: `400 Bad Request`.
  - El body contiene un array de errores devuelto por `express-openapi-validator` indicando `request/body must have required property 'contrasena'`.
- **Por qué importa:** Verifica que peticiones sucias nunca alcancen la lógica interna ni consuman recursos, muriendo directamente en la capa de frontera (Gateway).

### Escenario 2: Validación de Esquema OpenAPI (Registro)
- **Acción:** `POST /api/v1/auth/register` con payload incompleto.
- **Resultado Esperado:** Status `400 Bad Request` indicando qué campo falta (`nombres`, `apellidos`, etc.).

### Escenario 3: Enrutamiento de Registro Exitoso
- **Acción:** `POST /api/v1/auth/register` con payload completo y válido.
- **Mock Setup:** `registerUser` mockeado para retornar un objeto simulado `{"token": "fake-jwt", "email": "test@correo.com", "roles": ["ROLE_CLIENTE"]}`.
- **Resultado Esperado:**
  - Status Code: `201 Created`.
  - El `auth.client` fue llamado con los parámetros exactos proporcionados.

### Escenario 4: Enrutamiento de Login Exitoso
- **Acción:** `POST /api/v1/auth/login` con credenciales válidas.
- **Mock Setup:** `loginUser` mockeado para retornar sesión simulada.
- **Resultado Esperado:** 
  - Status Code: `200 OK`.
  - El token es retornado correctamente al cliente.

### Escenario 5: Manejo de Errores de Cliente Subyacente
- **Acción:** `POST /api/v1/auth/login` con credenciales inválidas.
- **Mock Setup:** `loginUser` mockeado para lanzar un `HttpError(401, "Credenciales incorrectas")`.
- **Resultado Esperado:**
  - Status Code: `401 Unauthorized`.
  - Formato JSON con estructura `timestamp`, `status`, `error` y `message`.
