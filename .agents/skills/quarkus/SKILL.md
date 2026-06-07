---
name: quarkus
description: Guía de desarrollo con Quarkus, Panache, RESTEasy, Hibernate
---

# Quarkus Skill - Cinestar Backend

## Stack

- Quarkus 3.x
- Java 17
- Hibernate ORM + Panache
- RESTEasy Reactive
- PostgreSQL 15 (puerto 5433)
- Flyway (Code-First)
- Maven

## Estructura del Proyecto

```
servicios/{service-name}/
├── src/main/java/pe/edu/utp/cinestar/{service}/
│   ├── {Service}Application.java
│   ├── controller/
│   │   └── {Controller}.java
│   ├── model/
│   │   ├── dto/
│   │   │   └── {Request/Response}.java
│   │   └── entity/
│   │       └── {Entity}.java
│   ├── repository/
│   │   └── {Repository}.java
│   └── service/
│       └── {Service}.java
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/
│       └── V1__init_schema.sql
├── pom.xml
└── Dockerfile
```

## Convenciones

- **Package base**: `pe.edu.utp.cinestar.{service_name}`
- **Entities**: Extender `PanacheEntity` (auto-genera `id`) o `@Entity` + `@Id`
- **Repositories**: Interfaz extendiendo `PanacheRepository<Entity>` o usar métodos estáticos de `PanacheEntity`
- **Controllers**: `@Path("/api/{service}")` + `@GET/@POST/@PUT/@DELETE`
- **DTOs**: Clases simples, `@Data` o record classes

## Panache Patterns

### Active Record (Entity con lógica)
```java
@Entity
public class Movie extends PanacheEntity {
    public String titulo;
    public static List<Movie> findByGenero(String genero) {
        return find("genero", genero).list();
    }
}
```

### Repository Pattern
```java
@ApplicationScoped
public class MovieRepository implements PanacheRepository<Movie> {
    public List<Movie> findByGenero(String genero) {
        return find("genero", genero).list();
    }
}
```

## Docker

```dockerfile
# Builder
FROM quarkus/quarkus-maven-image AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runner
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S quarkus && adduser -S quarkus -G quarkus
USER quarkus
COPY --from=builder /app/target/quarkus-app/ /app/
EXPOSE 8080
ENTRYPOINT ["/app/run.sh"]
```

## Configuración

```properties
# application.properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5433/${DB_NAME:db_movies}
quarkus.datasource.jdbc.username=postgres
quarkus.datasource.jdbc.password=root
quarkus.hibernate-orm.database.generation=none
quarkus.flyway.migrate=true
```
