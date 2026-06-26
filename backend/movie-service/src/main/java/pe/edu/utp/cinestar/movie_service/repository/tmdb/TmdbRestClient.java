package pe.edu.utp.cinestar.movie_service.repository.tmdb;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

/**
 * Cliente REST Reactivo para The Movie Database (TMDB).
 * La URL base y los timeouts se configuran en application.properties bajo el prefijo "quarkus.rest-client.tmdb-api".
 */
@RegisterRestClient(configKey = "tmdb-api")
@ClientHeaderParam(name = "Authorization", value = "Bearer ${tmdb.api.token}")
@ClientHeaderParam(name = "accept", value = "application/json")
public interface TmdbRestClient {

    /**
     * Busca películas por título.
     * Endpoint: GET /search/movie?query={query}&language=es-MX
     */
    @GET
    @Path("/search/movie")
    @Timeout(2000)
    @Retry(maxRetries = 2)
    Uni<JsonNode> searchMovies(@QueryParam("query") String query, 
                               @QueryParam("language") String language);

    /**
     * Obtiene el detalle de una película, incluyendo créditos, videos (trailers) e imágenes (posters).
     * Endpoint: GET /movie/{movie_id}?append_to_response=credits,videos,images&language=es-MX
     */
    @GET
    @Path("/movie/{movie_id}")
    @Timeout(2000)
    @Retry(maxRetries = 2)
    Uni<JsonNode> getMovieDetails(@PathParam("movie_id") int movieId, 
                                  @QueryParam("append_to_response") String appendToResponse,
                                  @QueryParam("language") String language);
}
