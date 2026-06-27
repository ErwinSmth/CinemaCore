package pe.edu.utp.cinestar.movie_service.controller;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import pe.edu.utp.cinestar.movie_service.WiremockTmdbResource;
import io.quarkus.test.security.TestSecurity;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
@QuarkusTestResource(WiremockTmdbResource.class)
class MovieControllerTest {

    @Test
    @TestSecurity(authorizationEnabled = false)
    void testSearchTmdbMovies() {
        // En TDD, este test fallará inicialmente porque MovieController retorna 501 NOT_IMPLEMENTED
        given()
          .queryParam("query", "Inception")
        .when()
          .get("/movies/tmdb/search")
        .then()
          .statusCode(200)
          .body(containsString("Inception"));
    }
}
