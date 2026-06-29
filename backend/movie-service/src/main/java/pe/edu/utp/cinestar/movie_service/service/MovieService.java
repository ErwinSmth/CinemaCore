package pe.edu.utp.cinestar.movie_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.cinestar.movie_service.exception.MovieNotFoundException;
import pe.edu.utp.cinestar.movie_service.exception.TMDBIntegrationException;
import pe.edu.utp.cinestar.movie_service.model.Movie;
import pe.edu.utp.cinestar.movie_service.model.dto.MovieCarteleraResponse;
import pe.edu.utp.cinestar.movie_service.model.dto.MovieDetailResponse;
import pe.edu.utp.cinestar.movie_service.model.dto.UpdateMovieRequest;
import pe.edu.utp.cinestar.movie_service.model.RestriccionEdad;
import pe.edu.utp.cinestar.movie_service.repository.MovieRepository;
import pe.edu.utp.cinestar.movie_service.repository.RestriccionEdadRepository;
import pe.edu.utp.cinestar.movie_service.repository.tmdb.TmdbRestClient;

import java.util.ArrayList;
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
            throw new TMDBIntegrationException(
                    "Error de comunicación o timeout con TMDB al buscar: " + query + ". Detalle: " + e.getMessage());
        }
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "cartelera", allEntries = true),
        @CacheEvict(value = "preestreno", allEntries = true)
    })
    public Long importMovie(Integer tmdbId) {
        JsonNode tmdbData;
        try {
            tmdbData = tmdbRestClient.getMovieDetails(tmdbId, "credits,videos,images", "es-MX");
        } catch (Exception e) {
            throw new TMDBIntegrationException(
                    "Fallo de comunicación con TMDB o servicio inalcanzable para el ID: " + tmdbId);
        }

        if (tmdbData == null || !tmdbData.has("id")) {
            throw new MovieNotFoundException("La película no existe en TMDB");
        }

        // Validacion de calidad
        if (!tmdbData.has("overview") || tmdbData.get("overview").asText().isBlank() ||
                !tmdbData.has("poster_path") || tmdbData.get("poster_path").isNull() ||
                !tmdbData.has("genres") || !tmdbData.get("genres").isArray() || tmdbData.get("genres").isEmpty()) {
            throw new IllegalArgumentException(
                    "La película seleccionada no cuenta con información suficiente en TMDB (faltan sinopsis, póster o géneros). Por favor, seleccione otra versión.");
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

        if (tmdbData.has("runtime") && !tmdbData.get("runtime").isNull()) {
            movie.setDuracionMin(tmdbData.get("runtime").asInt());
        }

        ObjectNode metadataNode = objectMapper.createObjectNode();

        if (tmdbData.has("poster_path") && !tmdbData.get("poster_path").isNull()) {
            metadataNode.put("posterPath", "https://image.tmdb.org/t/p/w500" + tmdbData.get("poster_path").asText());
        }
        if (tmdbData.has("backdrop_path") && !tmdbData.get("backdrop_path").isNull()) {
            metadataNode.put("backdropPath",
                    "https://image.tmdb.org/t/p/w1280" + tmdbData.get("backdrop_path").asText());
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
                    if (count >= 5)
                        break;
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
        String searchQuery = (search != null && !search.isBlank()) ? "%" + search + "%" : null;
        return movieRepository.findByStatusAndSearch(status, searchQuery);
    }

    public MovieDetailResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Película no encontrada en la BD local con ID: " + id));
        return toDetailResponse(movie);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "cartelera", allEntries = true),
        @CacheEvict(value = "preestreno", allEntries = true)
    })
    public Movie updateMovie(Long id, UpdateMovieRequest req) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Película no encontrada con ID: " + id));
        if (req.getTitle() != null)
            movie.setTitulo(req.getTitle());
        if (req.getOverview() != null)
            movie.setSinopsis(req.getOverview());
        if (req.getAgeRating() != null) {
            restriccionEdadRepository.findByCodigo(req.getAgeRating()).ifPresent(movie::setRestriccionEdad);
        }
        if (req.getStatus() != null)
            movie.setEstado(req.getStatus());
        return movieRepository.save(movie);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "cartelera", allEntries = true),
        @CacheEvict(value = "preestreno", allEntries = true)
    })
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Película no encontrada con ID: " + id));
        movie.setEstado("ELIMINADA");
        movieRepository.save(movie);
    }

    @Cacheable(value = "cartelera")
    public List<MovieCarteleraResponse> getCartelera(String genre, String search) {
        String genreJson = null;
        if (genre != null && !genre.isBlank()) {
            genreJson = "{\"generos\": [\"" + genre + "\"]}";
        }
        String searchQuery = (search != null && !search.isBlank()) ? "%" + search + "%" : null;
        List<Movie> cartelera = movieRepository.findCartelera(searchQuery, genreJson);
        return cartelera.stream().map(this::toCarteleraResponse).collect(Collectors.toList());
    }

    @Cacheable(value = "preestreno")
    public List<MovieCarteleraResponse> getPreEstrenos(String genre, String search) {
        String genreJson = null;
        if (genre != null && !genre.isBlank()) {
            genreJson = "{\"generos\": [\"" + genre + "\"]}";
        }
        String searchQuery = (search != null && !search.isBlank()) ? "%" + search + "%" : null;
        List<Movie> preEstrenos = movieRepository.findPreEstrenos(searchQuery, genreJson);
        return preEstrenos.stream().map(this::toCarteleraResponse).collect(Collectors.toList());
    }

    private MovieCarteleraResponse toCarteleraResponse(Movie m) {
        MovieCarteleraResponse dto = new MovieCarteleraResponse();
        dto.setId(m.getId());
        dto.setTitulo(m.getTitulo());
        dto.setEstado(m.getEstado());
        dto.setRestriccionEdad(m.getRestriccionEdad() != null ? m.getRestriccionEdad().getCodigo() : null);
        if (m.getMetadata() != null) {
            if (m.getMetadata().has("posterPath"))
                dto.setPosterPath(m.getMetadata().get("posterPath").asText());
            if (m.getMetadata().has("backdropPath"))
                dto.setBackdropPath(m.getMetadata().get("backdropPath").asText());
            if (m.getMetadata().has("trailers") && m.getMetadata().get("trailers").isArray()) {
                List<String> trailers = new ArrayList<>();
                m.getMetadata().get("trailers").forEach(t -> trailers.add(t.asText()));
                dto.setTrailers(trailers);
            }
        }
        if (m.getFechaEstreno() != null)
            dto.setFechaEstreno(m.getFechaEstreno());
        return dto;
    }

    private MovieDetailResponse toDetailResponse(Movie m) {
        MovieDetailResponse dto = new MovieDetailResponse();
        dto.setId(m.getId());
        dto.setTitulo(m.getTitulo());
        dto.setSinopsis(m.getSinopsis());
        dto.setDuracionMin(m.getDuracionMin());
        dto.setFechaEstreno(m.getFechaEstreno());
        dto.setEstado(m.getEstado());
        dto.setRestriccionEdad(m.getRestriccionEdad() != null ? m.getRestriccionEdad().getCodigo() : null);

        JsonNode meta = m.getMetadata();
        if (meta != null) {
            if (meta.has("posterPath"))
                dto.setPosterPath(meta.get("posterPath").asText());
            if (meta.has("backdropPath"))
                dto.setBackdropPath(meta.get("backdropPath").asText());
            if (meta.has("director"))
                dto.setDirector(meta.get("director").asText());

            if (meta.has("actores") && meta.get("actores").isArray()) {
                List<String> actores = new ArrayList<>();
                meta.get("actores").forEach(a -> actores.add(a.asText()));
                dto.setActores(actores);
            }
            if (meta.has("generos") && meta.get("generos").isArray()) {
                List<String> generos = new ArrayList<>();
                meta.get("generos").forEach(g -> generos.add(g.asText()));
                dto.setGeneros(generos);
            }
            if (meta.has("trailers") && meta.get("trailers").isArray()) {
                List<String> trailers = new ArrayList<>();
                meta.get("trailers").forEach(t -> trailers.add(t.asText()));
                dto.setTrailers(trailers);
            }
        }

        return dto;
    }
}
