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
@AutoConfigureMockMvc
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
    public void testScenario3_CacheAside_RepositoryLlamadoUnaSolaVez() throws Exception {
        // GET /movies es la ruta pública de cartelera (sin autenticación requerida)
        mockMvc.perform(get("/movies")).andExpect(status().isOk());
        mockMvc.perform(get("/movies")).andExpect(status().isOk());
        mockMvc.perform(get("/movies")).andExpect(status().isOk());

        // A pesar de 3 peticiones, el repositorio solo fue consultado 1 vez (caché hit en las 2 siguientes)
        Mockito.verify(movieRepository, Mockito.times(1)).findCartelera(any(), any());
    }
}
