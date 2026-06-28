package pe.edu.utp.cinestar.movie_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.cinestar.movie_service.model.Movie;
import pe.edu.utp.cinestar.movie_service.model.RestriccionEdad;
import pe.edu.utp.cinestar.movie_service.repository.MovieRepository;
import pe.edu.utp.cinestar.movie_service.repository.RestriccionEdadRepository;
import pe.edu.utp.cinestar.movie_service.repository.tmdb.TmdbRestClient;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MovieServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RestriccionEdadRepository restriccionEdadRepository;

    @MockitoBean
    private TmdbRestClient tmdbRestClient;

    // -------------------------------------------------------------------
    // Scenario 1: Búsqueda exitosa en TMDB (WireMock simulado con Mockito)
    // -------------------------------------------------------------------
    @Test
    public void testScenario1_TmdbSearchSuccess() throws Exception {
        com.fasterxml.jackson.databind.JsonNode mockNode =
                mapper.readTree("{\"results\": [{\"id\": 12345, \"title\": \"Inception\"}]}");
        when(tmdbRestClient.searchMovies(eq("Inception"), anyString()))
                .thenReturn(mockNode);

        mockMvc.perform(get("/movies/tmdb/search")
                        .header("X-User-Role", "ROLE_ADMINISTRADOR")
                        .param("query", "Inception"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.results[0].title", equalTo("Inception")))
                .andExpect(jsonPath("$.results[0].id", equalTo(12345)));
    }

    // -------------------------------------------------------------------
    // Scenario 2: Fallo de Integración Externa → 502 Bad Gateway
    // -------------------------------------------------------------------
    @Test
    public void testScenario2_TmdbFailureReturns502() throws Exception {
        when(tmdbRestClient.getMovieDetails(eq(999), anyString(), anyString()))
                .thenThrow(new RuntimeException("Simulated API Error"));

        mockMvc.perform(post("/movies/tmdb/import/999")
                        .header("X-User-Role", "ROLE_ADMINISTRADOR"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", equalTo(502)))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.path", equalTo("/movies/tmdb/import/999")));
    }

    // -------------------------------------------------------------------
    // Scenario 3: Importar película llena la BD y GET /movies/{id} la devuelve como DTO
    // -------------------------------------------------------------------
    @Test
    public void testScenario3_ImportAndGetById() throws Exception {
        String tmdbJsonRaw = "{"
                + "\"id\":550,\"title\":\"Fight Club\",\"overview\":\"Un hombre insomne\","
                + "\"runtime\":139,\"poster_path\":\"/somepost.jpg\",\"backdrop_path\":null,"
                + "\"genres\":[{\"name\":\"Drama\"}],"
                + "\"credits\":{\"cast\":[{\"name\":\"Brad Pitt\"},{\"name\":\"Edward Norton\"}],"
                + "\"crew\":[{\"job\":\"Director\",\"name\":\"David Fincher\"}]},"
                + "\"videos\":{\"results\":[{\"site\":\"YouTube\",\"type\":\"Trailer\",\"key\":\"SUXWAEX2jlg\"}]},"
                + "\"images\":{\"posters\":[]}"
                + "}";
        com.fasterxml.jackson.databind.JsonNode tmdbNode = mapper.readTree(tmdbJsonRaw);
        when(tmdbRestClient.getMovieDetails(eq(550), anyString(), anyString())).thenReturn(tmdbNode);

        // Asegurar restricción de edad base
        restriccionEdadRepository.findByCodigo("APT").orElseGet(() -> {
            RestriccionEdad r = new RestriccionEdad();
            r.setCodigo("APT");
            r.setDescripcion("Apto para todo público");
            return restriccionEdadRepository.save(r);
        });

        // Importar
        String importResponse = mockMvc.perform(post("/movies/tmdb/import/550")
                        .header("X-User-Role", "ROLE_ADMINISTRADOR"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long movieId = mapper.readTree(importResponse).get("id").asLong();

        // Consultar como usuario público (sin header)
        mockMvc.perform(get("/movies/" + movieId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.titulo", equalTo("Fight Club")))
                .andExpect(jsonPath("$.director", equalTo("David Fincher")))
                .andExpect(jsonPath("$.actores", hasItem("Brad Pitt")))
                .andExpect(jsonPath("$.generos", hasItem("Drama")))
                .andExpect(jsonPath("$.trailers[0]", containsString("youtube.com")))
                .andExpect(jsonPath("$.tmdbId").doesNotExist())    // No se expone tmdbId
                .andExpect(jsonPath("$.createdAt").doesNotExist()); // No se expone createdAt
    }

    // -------------------------------------------------------------------
    // Scenario 4: Soft Delete — BD conserva la fila con estado ELIMINADA
    // -------------------------------------------------------------------
    @Test
    public void testScenario4_SoftDelete() throws Exception {
        RestriccionEdad r = restriccionEdadRepository.findByCodigo("APT").orElseGet(() -> {
            RestriccionEdad re = new RestriccionEdad();
            re.setCodigo("APT");
            re.setDescripcion("Apto");
            return restriccionEdadRepository.save(re);
        });

        Movie testMovie = new Movie();
        testMovie.setTmdbId(777001);
        testMovie.setTitulo("Pelicula Para Borrar");
        testMovie.setEstado("PRE-ESTRENO");
        testMovie.setRestriccionEdad(r);
        Movie savedMovie = movieRepository.save(testMovie);
        Long idLocal = savedMovie.getId();

        mockMvc.perform(delete("/movies/" + idLocal)
                        .header("X-User-Role", "ROLE_ADMINISTRADOR"))
                .andExpect(status().isNoContent());

        Movie deletedMovie = movieRepository.findById(idLocal).orElseThrow();
        assertEquals("ELIMINADA", deletedMovie.getEstado(),
                "La película debe permanecer en DB pero con estado ELIMINADA");
    }

    // -------------------------------------------------------------------
    // Scenario 5: Acceso a ruta privada sin rol → 403 Forbidden
    // -------------------------------------------------------------------
    @Test
    public void testScenario5_RutaPrivadaSinRolRetorna403() throws Exception {
        mockMvc.perform(post("/movies/tmdb/import/12345"))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------
    // Scenario 6: GET /movies/{id} no encontrado → 404 con JSON estándar
    // -------------------------------------------------------------------
    @Test
    public void testScenario6_MovieNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/movies/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", equalTo(404)))
                .andExpect(jsonPath("$.path", equalTo("/movies/999999")));
    }
}
