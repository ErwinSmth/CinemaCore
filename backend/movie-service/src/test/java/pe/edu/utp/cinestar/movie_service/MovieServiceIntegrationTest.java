package pe.edu.utp.cinestar.movie_service;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import pe.edu.utp.cinestar.movie_service.model.Movie;
import pe.edu.utp.cinestar.movie_service.repository.MovieRepository;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(WiremockTmdbResource.class)
public class MovieServiceIntegrationTest {

    @Inject
    MovieRepository movieRepository;

    @Test
    @TestSecurity(user = "admin", roles = {"ROLE_ADMINISTRADOR"})
    @JwtSecurity(claims = {@Claim(key = "email", value = "admin@cinestar.pe")})
    public void testScenario1_TmdbSearchSuccess() {
        given()
            .queryParam("query", "Inception")
            .when().get("/movies/tmdb/search")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("results[0].title", equalTo("Inception"))
            .body("results[0].id", equalTo(12345));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ROLE_ADMINISTRADOR"})
    @JwtSecurity(claims = {@Claim(key = "email", value = "admin@cinestar.pe")})
    public void testScenario2_TmdbFailureReturns502() {
        given()
            .when().post("/movies/tmdb/import/999") // Este ID está mockeado para devolver 500
            .then()
            .statusCode(502) // Bad Gateway
            .contentType(ContentType.JSON)
            .body("status", equalTo(502))
            .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ROLE_ADMINISTRADOR"})
    @JwtSecurity(claims = {@Claim(key = "email", value = "admin@cinestar.pe")})
    public void testScenario4_SoftDelete() {
        // Preparar DB: Insertar película a mano para luego borrarla
        Movie testMovie = new Movie();
        testMovie.tmdbId = 101010;
        testMovie.titulo = "Pelicula Para Borrar";
        testMovie.estado = "PRE-ESTRENO";
        pe.edu.utp.cinestar.movie_service.model.RestriccionEdad r = new pe.edu.utp.cinestar.movie_service.model.RestriccionEdad();
        r.id = 1;
        testMovie.restriccionEdad = r;
        
        insertMovieForTest(testMovie);
        Long idLocal = testMovie.id;

        // Ejecutar Soft Delete via REST
        given()
            .when().delete("/movies/" + idLocal)
            .then()
            .statusCode(204); // No Content

        // Validar directamente en DB
        Movie deletedMovie = movieRepository.findById(idLocal);
        assertEquals("ELIMINADA", deletedMovie.estado, "La película debe permanecer en DB pero con estado ELIMINADA");
    }

    @Transactional
    void insertMovieForTest(Movie movie) {
        movieRepository.persist(movie);
    }
}
