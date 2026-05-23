# Cinestar Backend Platform

Plataforma backend basada en microservicios para el proyecto Cinestar.

## Arquitectura
Este proyecto sigue un enfoque **Monorepo** para centralizar la lógica, la infraestructura y las configuraciones globales.

### Estructura
- `/servicios`: Contiene los microservicios individuales (Auth, Movie, etc.).
- `/infra`: Contiene el orquestador `docker-compose.yml` para la base de datos centralizada.

## Prerrequisitos
- Docker y Docker Compose Instalados
- Java 17

## Instrucciones de Inicio Rapido
1. Levantar la infraestructura (Base de Datos):
    ```bash
   cd cinestar-infra
   docker-compose up -d

2. Ejecutar en un servicio individual:
    ```bash
    cd servicios/auth-service
    .\mvnw clean spring-boot:run