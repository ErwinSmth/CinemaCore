# Cinestar Backend Platform

Plataforma backend basada en microservicios para el proyecto Cinestar (Proyecto Académico).

## Arquitectura

Este proyecto sigue un enfoque **Monorepo** y **Políglota**, centralizando la lógica de negocio y la infraestructura.

### Componentes

- **Orquestador / BFF (Backend For Frontend):** Implementado con **Node.js, TypeScript y Express**. Actúa como el punto de entrada, gestionando la seguridad (JWT) y la orquestación de llamadas entre servicios.
- **Microservicios Core:** Implementados con **Java 17 (Spring Boot)** para lógica transaccional de alta consistencia.
- **Infraestructura:** Orquestación centralizada mediante `docker-compose`.

### Estructura

- `/servicios`: Contiene los microservicios individuales (API Gateway, Auth, Movie, etc.).
- `/infra`: Contiene el orquestador `docker-compose.yml` para la base de datos centralizada.

## Prerrequisitos

- Docker y Docker Compose
- Java 17 (Para servicios Spring Boot)
- Node.js (LTS recomendado, para API Gateway)

## Instrucciones de Inicio Rápido

### 1. Levantar la infraestructura (Base de Datos)

```bash
cd infra
docker-compose up -d
```

### 2. Ejecutar el Servicio de Autenticacion

Puedes ejecutar este microservicio de dos maneras distintas según tu necesidad:

Opcion A: Modo Desarrollo Local
Ideal para escribir codigo, se ejecuta por defecto en el puerto 8080

```bash
cd servicios/auth-service
.\mvnw clean spring-boot:run
```

Opcion B: Modo Contenedor(Docker)

Ideal para simular el entorno de produccion. Utiliza un DOckerfile que expone el servicio en el puerto 8081 de la maquina Host

```bash
cd servicios/auth-service

# 1. Construir la imagen Docker
docker build -t cinestar-auth-service .

# 2. Ejecutar el contenedor (Mapeando el puerto 8081 al 8080 interno)
docker run -d -p 8081:8080 --name auth-container cinestar-auth-service
```

### 3. Configurar y Ejecutar el API Gateway (Node.js)

El Gateway necesita apuntar al puerto correcto dependiendo de cómo levantaste el Auth Service en el paso anterior.

Navega a `servicios/api-gateway/` y asegúrate de configurar tu archivo `.env` correctamente:

**Si usaste la Opción A (Nativo):**
```env
PORT=3000
AUTH_SERVICE_URL=http://localhost:8080
```

**Si usaste la Opción B (Docker):**
```env
PORT=3000
AUTH_SERVICE_URL=http://localhost:8081
```

Instala dependencias y levanta el servidor:

```bash
cd servicios/api-gateway
npm install
npm run dev
```

El API Gateway estará escuchando en `http://localhost:3000`. Todas las peticiones del Frontend (como `/auth/login`) deben dirigirse a este puerto.

## 📡 Endpoints Expuestos (API Gateway)

| Método | Endpoint | Descripción | Body (JSON) |
|---|---|---|---|
| `GET` | `/health` | Verifica el estado del API Gateway | - |
| `POST` | `/auth/login` | Inicia sesión y retorna JWT | `{ "email": "...", "contrasena": "..." }` |

## 🛠️ Tecnologías Utilizadas

### Backend Core (Java)
- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL

### API Gateway / BFF (Node.js)
- Node.js (v20+)
- TypeScript
- Express.js
- Axios (Cliente HTTP Interno)
- JSON Web Token (JWT)

### Infraestructura
- Docker & Docker Compose
- Alpine Linux (Base Images)
