package pe.edu.utp.cinestar.movie_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.cinestar.movie_service.model.Movie;
import pe.edu.utp.cinestar.movie_service.repository.MovieRepository;
import pe.edu.utp.cinestar.movie_service.repository.tmdb.TmdbRestClient;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MovieServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private pe.edu.utp.cinestar.movie_service.repository.RestriccionEdadRepository restriccionEdadRepository;

    @MockitoBean
    private TmdbRestClient tmdbRestClient;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testScenario1_TmdbSearchSuccess() throws Exception {
        com.fasterxml.jackson.databind.JsonNode mockNode = mapper.readTree("{\"results\": [{\"id\": 12345, \"title\": \"Inception\"}]}");
        when(tmdbRestClient.searchMovies(eq("Inception"), anyString()))
                .thenReturn(mockNode);

        mockMvc.perform(get("/movies/tmdb/search")
                .param("query", "Inception"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.results[0].title", equalTo("Inception")))
                .andExpect(jsonPath("$.results[0].id", equalTo(12345)));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testScenario2_TmdbFailureReturns502() throws Exception {
        when(tmdbRestClient.getMovieDetails(eq(999), anyString(), anyString()))
                .thenThrow(new RuntimeException("Simulated API Error"));

        mockMvc.perform(post("/movies/tmdb/import/999"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", equalTo(502)))
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    public void testScenario4_SoftDelete() throws Exception {
        Movie testMovie = new Movie();
        testMovie.setTmdbId(101010);
        testMovie.setTitulo("Pelicula Para Borrar");
        testMovie.setEstado("PRE-ESTRENO");
        pe.edu.utp.cinestar.movie_service.model.RestriccionEdad r = new pe.edu.utp.cinestar.movie_service.model.RestriccionEdad();
        r.setCodigo("APT");
        r.setDescripcion("Apto");
        r = restriccionEdadRepository.save(r);
        testMovie.setRestriccionEdad(r);

        Movie savedMovie = movieRepository.save(testMovie);
        Long idLocal = savedMovie.getId();

        mockMvc.perform(delete("/movies/" + idLocal))
                .andExpect(status().isNoContent());

        Movie deletedMovie = movieRepository.findById(idLocal).orElseThrow();
        assertEquals("ELIMINADA", deletedMovie.getEstado(), "La película debe permanecer en DB pero con estado ELIMINADA");
    }
}
