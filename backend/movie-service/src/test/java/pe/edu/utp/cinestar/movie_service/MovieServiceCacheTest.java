package pe.edu.utp.cinestar.movie_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.cinestar.movie_service.repository.MovieRepository;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MovieServiceCacheTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieRepository movieRepository;

    @BeforeEach
    public void setup() {
        Mockito.when(movieRepository.findCartelera(any(), any())).thenReturn(Collections.emptyList());
    }

    @Test
    public void testScenario3_RedisCacheAside() throws Exception {
        mockMvc.perform(get("/movies/cartelera")).andExpect(status().isOk());
        mockMvc.perform(get("/movies/cartelera")).andExpect(status().isOk());
        mockMvc.perform(get("/movies/cartelera")).andExpect(status().isOk());

        Mockito.verify(movieRepository, Mockito.times(1)).findCartelera(any(), any());
    }
}
