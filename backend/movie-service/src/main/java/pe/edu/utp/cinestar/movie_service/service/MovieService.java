package pe.edu.utp.cinestar.movie_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import pe.edu.utp.cinestar.movie_service.repository.tmdb.TmdbRestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class MovieService {

    @Inject
    @RestClient
    TmdbRestClient tmdbRestClient;

    @Inject
    ObjectMapper objectMapper;

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

        Movie movie = Movie.findByTmdbId(tmdbId);
        if (movie == null) {
            movie = new Movie();
            movie.tmdbId = tmdbId;
            movie.estado = "INACTIVO"; // Estado inicial por defecto
            movie.restriccionEdad = RestriccionEdad.findByCodigo("APT"); // Restricción inicial por defecto
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

        movie.persist();
        return movie.id;
    }

    public List<Movie> getAllMoviesAdmin(String status, String search) {
        StringBuilder hql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        
        if (status != null && !status.trim().isEmpty()) {
            hql.append(" and estado = :status");
            params.put("status", status.trim());
        }
        
        if (search != null && !search.trim().isEmpty()) {
            hql.append(" and titulo ilike :search");
            params.put("search", "%" + search.trim() + "%");
        }
        
        return Movie.find(hql.toString(), params).list();
    }

    public Movie getMovieById(Long id) {
        Movie movie = Movie.findById(id);
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
            RestriccionEdad res = RestriccionEdad.findByCodigo(req.getAgeRating());
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
        StringBuilder sql = new StringBuilder("SELECT * FROM peliculas WHERE estado = 'CARTELERA'");
        Map<String, Object> params = new HashMap<>();
        
        if (search != null && !search.trim().isEmpty()) {
            // Uso de ILIKE para aprovechar el índice pg_trgm de PostgreSQL de manera segura
            sql.append(" AND titulo ILIKE :search");
            params.put("search", "%" + search.trim() + "%");
        }
        
        if (genre != null && !genre.trim().isEmpty()) {
            // Consulta GIN nativa usando el operador de contención JSONB @>
            sql.append(" AND metadata @> CAST(:genreJson AS jsonb)");
            params.put("genreJson", "{\"generos\": [\"" + genre.trim() + "\"]}");
        }
        
        // Ejecutamos la consulta nativa de forma segura y parametrizada contra SQL Injection
        jakarta.persistence.Query nativeQuery = Movie.getEntityManager().createNativeQuery(sql.toString(), Movie.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            nativeQuery.setParameter(entry.getKey(), entry.getValue());
        }
        
        @SuppressWarnings("unchecked")
        List<Movie> cartelera = nativeQuery.getResultList();
        
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
