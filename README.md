# Cinestar Backend Platform

Plataforma backend basada en microservicios para el proyecto Cinestar.

## Arquitectura

Este proyecto sigue un enfoque **Monorepo** para centralizar la lógica, la infraestructura y las configuraciones globales.

### Componentes

- **Orquestador:** Implementado con **Node.js, TypeScript y Express**. Actúa como el punto de entrada, gestionando la seguridad (JWT) y la orquestación de llamadas entre servicios.
- **Microservicios Core:** Implementados con **Java 17 (Spring Boot)** para lógica transaccional de alta consistencia.
- **Infraestructura:** Orquestación centralizada mediante `docker-compose`.

### Estructura

- `/servicios`: Contiene los microservicios individuales (API Gateway, Auth, Movie, etc.).
- `/infra`: Contiene el orquestador `docker-compose.yml` para la base de datos centralizada.

## Prerrequisitos

- Docker y Docker Compose
- Java 17 (Para servicios Spring Boot)
- Node.js (LTS recomendado, para API Gateway)

## Instrucciones de Inicio Rapido

### 1. Levantar la infraestructura (Base de Datos)

```bash
cd infra
docker-compose up -d
```

### 2. Ejecutar el API Gateway (Node.js)

```bash
cd servicios/api-gateway
npm install
npm run dev
```

### 3. Ejecutar un servicio individual (Java)

```bash
cd servicios/auth-service
.\mvnw clean spring-boot:run
```
