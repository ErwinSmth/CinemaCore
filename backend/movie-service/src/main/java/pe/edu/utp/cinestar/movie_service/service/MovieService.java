package pe.edu.utp.cinestar.movie_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@RequiredArgsConstructor
public class MovieService {

    private final TmdbRestClient tmdbRestClient;
    private final ObjectMapper objectMapper;
    private final MovieRepository movieRepository;
    private final RestriccionEdadRepository restriccionEdadRepository;

    public JsonNode searchTmdbMovies(String query) {
        try {
            return tmdbRestClient.searchMovies(query, "es-MX");
        } catch (Exception e) {
            throw new TMDBIntegrationException("Error de comunicación o timeout con TMDB al buscar: " + query);
        }
    }

    @Transactional
    @CacheEvict(value = "cartelera", allEntries = true)
    public Long importMovie(Integer tmdbId) {
        JsonNode tmdbData;
        try {
            tmdbData = tmdbRestClient.getMovieDetails(tmdbId, "credits,videos,images", "es-MX");
        } catch (Exception e) {
            throw new TMDBIntegrationException("Fallo de comunicación con TMDB o servicio inalcanzable para el ID: " + tmdbId);
        }

        if (tmdbData == null || !tmdbData.has("id")) {
            throw new MovieNotFoundException("La película no existe en TMDB");
        }

        Movie movie = movieRepository.findByTmdbId(tmdbId).orElse(null);
        if (movie == null) {
            movie = new Movie();
            movie.setTmdbId(tmdbId);
            movie.setEstado("INACTIVO");
            movie.setRestriccionEdad(restriccionEdadRepository.findByCodigo("APT").orElse(null));
        }

        movie.setTitulo(tmdbData.has("title") ? tmdbData.get("title").asText() : "");
        movie.setSinopsis(tmdbData.has("overview") ? tmdbData.get("overview").asText() : "");
        
        ObjectNode metadataNode = objectMapper.createObjectNode();

        if (tmdbData.has("poster_path") && !tmdbData.get("poster_path").isNull()) {
            metadataNode.put("posterPath", "https://image.tmdb.org/t/p/w500" + tmdbData.get("poster_path").asText());
        }
        if (tmdbData.has("backdrop_path") && !tmdbData.get("backdrop_path").isNull()) {
            metadataNode.put("backdropPath", "https://image.tmdb.org/t/p/w1280" + tmdbData.get("backdrop_path").asText());
        }

        ArrayNode genresNode = metadataNode.putArray("generos");
        if (tmdbData.has("genres") && tmdbData.get("genres").isArray()) {
            tmdbData.get("genres").forEach(g -> genresNode.add(g.get("name").asText()));
        }

        if (tmdbData.has("credits")) {
            JsonNode credits = tmdbData.get("credits");
            ArrayNode castNode = metadataNode.putArray("actores");
            if (credits.has("cast") && credits.get("cast").isArray()) {
                int count = 0;
                for (JsonNode actor : credits.get("cast")) {
                    if (count >= 5) break;
                    castNode.add(actor.get("name").asText());
                    count++;
                }
            }

            if (credits.has("crew") && credits.get("crew").isArray()) {
                for (JsonNode crew : credits.get("crew")) {
                    if ("Director".equals(crew.get("job").asText())) {
                        metadataNode.put("director", crew.get("name").asText());
                        break;
                    }
                }
            }
        }

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

        movie.setMetadata(metadataNode);
        movieRepository.save(movie);
        return movie.getId();
    }

    public List<Movie> getAllMoviesAdmin(String status, String search) {
        return movieRepository.findByStatusAndSearch(status, search);
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException("Película no encontrada en la BD local con ID: " + id));
    }

    @Transactional
    @CacheEvict(value = "cartelera", allEntries = true)
    public Movie updateMovie(Long id, UpdateMovieRequest req) {
        Movie movie = getMovieById(id);
        if (req.getTitle() != null) movie.setTitulo(req.getTitle());
        if (req.getOverview() != null) movie.setSinopsis(req.getOverview());
        if (req.getAgeRating() != null) {
            restriccionEdadRepository.findByCodigo(req.getAgeRating()).ifPresent(movie::setRestriccionEdad);
        }
        if (req.getStatus() != null) movie.setEstado(req.getStatus());
        return movieRepository.save(movie);
    }

    @Transactional
    @CacheEvict(value = "cartelera", allEntries = true)
    public void deleteMovie(Long id) {
        Movie movie = getMovieById(id);
        movie.setEstado("ELIMINADA");
        movieRepository.save(movie);
    }

    @Cacheable(value = "cartelera")
    public List<MovieCarteleraResponse> getCartelera(String genre, String search) {
        List<Movie> cartelera = movieRepository.findCartelera(genre, search);
        
        return cartelera.stream().map(m -> {
            MovieCarteleraResponse dto = new MovieCarteleraResponse();
            dto.setId(m.getId());
            dto.setTitulo(m.getTitulo());
            if (m.getMetadata() != null && m.getMetadata().has("posterPath")) {
                dto.setPosterPath(m.getMetadata().get("posterPath").asText());
            }
            dto.setEstado(m.getEstado());
            dto.setRestriccionEdad(m.getRestriccionEdad() != null ? m.getRestriccionEdad().getCodigo() : null);
            return dto;
        }).collect(Collectors.toList());
    }
}
