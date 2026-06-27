package pe.edu.utp.cinestar.movie_service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pe.edu.utp.cinestar.movie_service.repository.MovieRepository;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class MovieServiceCacheTest {

    @InjectMock
    MovieRepository movieRepository;

    @BeforeEach
    public void setup() {
        // Configuramos el mock para que devuelva una lista vacía por defecto
        Mockito.when(movieRepository.findCartelera(any(), any())).thenReturn(Collections.emptyList());
    }

    @Test
    public void testScenario3_RedisCacheAside() {
        // Ejecutamos la petición 3 veces
        given().when().get("/movies").then().statusCode(200);
        given().when().get("/movies").then().statusCode(200);
        given().when().get("/movies").then().statusCode(200);

        // Verificamos que el repositorio solo fue llamado UNA vez (las otras dos salieron de Redis)
        Mockito.verify(movieRepository, Mockito.times(1)).findCartelera(any(), any());
    }
}
