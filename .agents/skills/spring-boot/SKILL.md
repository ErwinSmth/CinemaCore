---
name: spring-boot
description: Guía de desarrollo con Spring Boot 4, JPA, Flyway, JWT, Spring Security
---

# Spring Boot Skill - Cinestar Backend

## Stack

- Spring Boot 4.0.6
- Java 17
- Spring Data JPA + Hibernate
- Spring Security + JWT (jjwt 0.11.5)
- PostgreSQL 15 (puerto 5433)
- Flyway (Code-First)
- Lombok
- Maven

## Estructura del Proyecto

```
servicios/{service-name}/
├── src/main/java/pe/edu/utp/cinestar/{service}/
│   ├── {Service}Application.java
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   └── {Controller}.java
│   ├── model/
│   │   ├── dto/
│   │   │   └── {Request/Response}.java
│   │   └── entity/
│   │       └── {Entity}.java
│   ├── repository/
│   │   └── {Repository}.java
│   ├── service/
│   │   └── {Service}.java
│   ├── security/
│   │   ├── JwtProvider.java
│   │   └── JwtAuthenticationFilter.java
│   └── exception/
│       ├── ErrorResponse.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/
│       └── V1__init_schema.sql
├── pom.xml
└── Dockerfile
```

## Convenciones

- **Package base**: `pe.edu.utp.cinestar.{service_name}`
- **Entities**: `@Entity` + `@Data` (Lombok) + `@Table(name="table_name")`
- **DTOs**: Clases simples con `@Data`, validación con `@NotBlank`, `@Email`, `@Size`
- **Repositories**: Extender `JpaRepository<Entity, IdType>`
- **Services**: `@Service` + `@Transactional` para write operations
- **Controllers**: `@RestController` + `@RequestMapping("/api/{service}")`
- **Error handling**: `@RestControllerAdvice` + `GlobalExceptionHandler`

## Seguridad

- JWT con `JwtProvider` (generar, validar, extraer email)
- `JwtAuthenticationFilter` como `OncePerRequestFilter`
- `SecurityConfig`: BCrypt, DaoAuthenticationProvider, stateless session
- RBAC: `@PreAuthorize("hasRole('ADMIN')")` o `SecurityContext`

## Migraciones Flyway

- Nombre: `V{version}__{description}.sql`
- Code-First: Crear SQL manualmente, no usar `ddl-auto`
- `spring.jpa.hibernate.ddl-auto=none`
- `spring.flyway.enabled=true`

## Docker

```dockerfile
# Builder
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runner
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms64m", "-Xmx128m", "-jar", "app.jar"]
```

## Patrones Comunes

### Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ErrorResponse(...));
    }
}
```

### JWT Provider
```java
@Component
public class JwtProvider {
    public String generate(String email, Set<String> roles) { ... }
    public boolean validate(String token) { ... }
    public String extractEmail(String token) { ... }
}
```
