package pe.edu.utp.cinestar.movie_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import pe.edu.utp.cinestar.movie_service.exception.MovieNotFoundException;
import pe.edu.utp.cinestar.movie_service.exception.TMDBIntegrationException;
import pe.edu.utp.cinestar.movie_service.model.Movie;
import pe.edu.utp.cinestar.movie_service.model.dto.MovieCarteleraResponse;
import pe.edu.utp.cinestar.movie_service.model.dto.UpdateMovieRequest;
import pe.edu.utp.cinestar.movie_service.model.RestriccionEdad;
import pe.edu.utp.cinestar.movie_service.repository.MovieRepository;
import pe.edu.utp.cinestar.movie_service.repository.RestriccionEdadRepository;
import pe.edu.utp.cinestar.movie_service.repository.tmdb.TmdbRestClient;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MovieService {

    @Inject
    @RestClient
    TmdbRestClient tmdbRestClient;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    MovieRepository movieRepository;

    @Inject
    RestriccionEdadRepository restriccionEdadRepository;

    public JsonNode searchTmdbMovies(String query) {
        try {
            return tmdbRestClient.searchMovies(query, "es-MX").await().indefinitely();
        } catch (Exception e) {
            throw new TMDBIntegrationException("Error de comunicación o timeout con TMDB al buscar: " + query);
        }
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "cartelera")
    public Long importMovie(Integer tmdbId) {
        // Obtenemos los datos frescos de TMDB (Incluyendo créditos y videos)
        JsonNode tmdbData;
        try {
            tmdbData = tmdbRestClient.getMovieDetails(tmdbId, "credits,videos,images", "es-MX").await()
                    .indefinitely();
        } catch (Exception e) {
            throw new TMDBIntegrationException("Fallo de comunicación con TMDB o servicio inalcanzable para el ID: " + tmdbId);
        }

        if (tmdbData == null || !tmdbData.has("id")) {
            throw new MovieNotFoundException("La película no existe en TMDB");
        }

        Movie movie = movieRepository.findByTmdbId(tmdbId);
        if (movie == null) {
            movie = new Movie();
            movie.tmdbId = tmdbId;
            movie.estado = "INACTIVO"; // Estado inicial por defecto
            movie.restriccionEdad = restriccionEdadRepository.findByCodigo("APT"); // Restricción inicial por defecto
        }

        // Upsert Híbrido: Siempre actualizamos los metadatos visuales
        movie.titulo = tmdbData.has("title") ? tmdbData.get("title").asText() : "";
        movie.sinopsis = tmdbData.has("overview") ? tmdbData.get("overview").asText() : "";
        // --- Construcción del JSONB Metadata ---
        ObjectNode metadataNode = objectMapper.createObjectNode();

        // 1. Imágenes (URLs Reconstruidas)
        if (tmdbData.has("poster_path") && !tmdbData.get("poster_path").isNull()) {
            metadataNode.put("posterPath", "https://image.tmdb.org/t/p/w500" + tmdbData.get("poster_path").asText());
        }
        if (tmdbData.has("backdrop_path") && !tmdbData.get("backdrop_path").isNull()) {
            metadataNode.put("backdropPath",
                    "https://image.tmdb.org/t/p/w1280" + tmdbData.get("backdrop_path").asText());
        }

        // 2. Géneros
        ArrayNode genresNode = metadataNode.putArray("generos");
        if (tmdbData.has("genres") && tmdbData.get("genres").isArray()) {
            tmdbData.get("genres").forEach(g -> genresNode.add(g.get("name").asText()));
        }

        // 3. Actores y Director
        if (tmdbData.has("credits")) {
            JsonNode credits = tmdbData.get("credits");

            // Top 5 Actores
            ArrayNode castNode = metadataNode.putArray("actores");
            if (credits.has("cast") && credits.get("cast").isArray()) {
                int count = 0;
                for (JsonNode actor : credits.get("cast")) {
                    if (count >= 5)
                        break;
                    castNode.add(actor.get("name").asText());
                    count++;
                }
            }

            // Director
            if (credits.has("crew") && credits.get("crew").isArray()) {
                for (JsonNode crew : credits.get("crew")) {
                    if ("Director".equals(crew.get("job").asText())) {
                        metadataNode.put("director", crew.get("name").asText());
                        break; // Solo tomamos al primer director
                    }
                }
            }
        }

        // 4. Trailers (YouTube)
        ArrayNode trailersNode = metadataNode.putArray("trailers");
        if (tmdbData.has("videos") && tmdbData.get("videos").has("results")) {
            JsonNode videos = tmdbData.get("videos").get("results");
            if (videos.isArray()) {
                for (JsonNode video : videos) {
                    if ("YouTube".equals(video.get("site").asText()) && "Trailer".equals(video.get("type").asText())) {
                        trailersNode.add("https://www.youtube.com/watch?v=" + video.get("key").asText());
                    }
                }
            }
        }

        // Asignamos el JSONB construido a la entidad
        movie.metadata = metadataNode;

        movieRepository.persist(movie);
        return movie.id;
    }

    public List<Movie> getAllMoviesAdmin(String status, String search) {
        return movieRepository.findByStatusAndSearch(status, search);
    }

    public Movie getMovieById(Long id) {
        Movie movie = movieRepository.findById(id);
        if (movie == null) {
            throw new MovieNotFoundException("Película no encontrada en la BD local con ID: " + id);
        }
        return movie;
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "cartelera")
    public Movie updateMovie(Long id, UpdateMovieRequest req) {
        Movie movie = getMovieById(id);
        if (req.getTitle() != null)
            movie.titulo = req.getTitle();
        if (req.getOverview() != null)
            movie.sinopsis = req.getOverview();
        if (req.getAgeRating() != null) {
            RestriccionEdad res = restriccionEdadRepository.findByCodigo(req.getAgeRating());
            if (res != null)
                movie.restriccionEdad = res;
        }
        if (req.getStatus() != null)
            movie.estado = req.getStatus();

        return movie;
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "cartelera")
    public void deleteMovie(Long id) {
        Movie movie = getMovieById(id);
        // Soft delete
        movie.estado = "ELIMINADA";
    }

    @CacheResult(cacheName = "cartelera")
    public List<MovieCarteleraResponse> getCartelera(String genre, String search) {
        List<Movie> cartelera = movieRepository.findCartelera(genre, search);
        
        return cartelera.stream().map(m -> {
            MovieCarteleraResponse dto = new MovieCarteleraResponse();
            dto.setId(m.id);
            dto.setTitulo(m.titulo);
            if (m.metadata != null && m.metadata.has("posterPath")) {
                dto.setPosterPath(m.metadata.get("posterPath").asText());
            }
            dto.setEstado(m.estado);
            dto.setRestriccionEdad(m.restriccionEdad != null ? m.restriccionEdad.codigo : null);
            return dto;
        }).collect(Collectors.toList());
    }
}
