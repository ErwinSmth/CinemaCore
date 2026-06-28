package pe.edu.utp.cinestar.movie_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.cinestar.movie_service.service.MovieService;
import pe.edu.utp.cinestar.movie_service.security.JwtProvider;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MovieControllerTest extends pe.edu.utp.cinestar.movie_service.AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper mapper;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void testSearchTmdbMovies() throws Exception {
        com.fasterxml.jackson.databind.JsonNode mockNode = mapper.readTree("{\"results\": [{\"title\": \"Inception\"}]}");
        when(movieService.searchTmdbMovies(anyString())).thenReturn(mockNode);

        mockMvc.perform(get("/movies/tmdb/search")
                .param("query", "Inception"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Inception")));
    }
}
