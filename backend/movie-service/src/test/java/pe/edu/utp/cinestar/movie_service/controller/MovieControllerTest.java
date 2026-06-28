package pe.edu.utp.cinestar.movie_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.cinestar.movie_service.AbstractIntegrationTest;
import pe.edu.utp.cinestar.movie_service.service.MovieService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MovieControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private MovieService movieService;

    // ----------------------------------------------------------------
    // Rutas PÚBLICAS — No requieren header X-User-Role
    // ----------------------------------------------------------------

    @Test
    void getCartelera_sinAutenticacion_retorna200() throws Exception {
        when(movieService.getCartelera(null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCartelera_conFiltros_retorna200() throws Exception {
        when(movieService.getCartelera(anyString(), anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/movies")
                        .param("genre", "Acción")
                        .param("search", "Spider"))
                .andExpect(status().isOk());
    }

    // ----------------------------------------------------------------
    // Rutas PRIVADAS — Deben rechazar si no hay X-User-Role
    // ----------------------------------------------------------------

    @Test
    void searchTmdb_sinHeader_retorna403() throws Exception {
        mockMvc.perform(get("/movies/tmdb/search")
                        .param("query", "Inception"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAdmin_sinHeader_retorna403() throws Exception {
        mockMvc.perform(get("/movies/admin"))
                .andExpect(status().isForbidden());
    }

    // ----------------------------------------------------------------
    // Rutas PRIVADAS — Funcionan correctamente con X-User-Role
    // ----------------------------------------------------------------

    @Test
    void searchTmdb_conHeaderAdministrador_retorna200() throws Exception {
        JsonNode mockNode = mapper.readTree("{\"results\": [{\"id\": 12345, \"title\": \"Inception\"}]}");
        when(movieService.searchTmdbMovies("Inception")).thenReturn(mockNode);

        mockMvc.perform(get("/movies/tmdb/search")
                        .header("X-User-Role", "ROLE_ADMINISTRADOR")
                        .param("query", "Inception"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Inception")));
    }

    @Test
    void searchTmdb_conRolCliente_retorna403() throws Exception {
        mockMvc.perform(get("/movies/tmdb/search")
                        .header("X-User-Role", "ROLE_CLIENTE")
                        .param("query", "Inception"))
                .andExpect(status().isForbidden());
    }
}
