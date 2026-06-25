package pe.edu.utp.cinestar.movie_service.controller;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import pe.edu.utp.cinestar.movie_service.config.TmdbWiremockResource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
@QuarkusTestResource(TmdbWiremockResource.class)
class MovieControllerTest {

    @Test
    void testSearchTmdbMovies() {
        // En TDD, este test fallará inicialmente porque MovieController retorna 501 NOT_IMPLEMENTED
        given()
          .queryParam("query", "Fight Club")
        .when()
          .get("/api/v1/movies/tmdb/search")
        .then()
          .statusCode(200)
          .body(containsString("Fight Club"));
    }
}
