package pe.edu.utp.cinestar.movie_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import pe.edu.utp.cinestar.movie_service.repository.tmdb.TmdbRestClient;

@ApplicationScoped
public class MovieService {

    @Inject
    @RestClient
    TmdbRestClient tmdbRestClient;

    public JsonNode searchTmdbMovies(String query) {
        // En un entorno reactivo podemos devolver Uni<JsonNode>
        // pero para simplificar la integración inicial, usamos el await() 
        // o si todo el stack es reactivo, lo dejamos como Uni
        return tmdbRestClient.searchMovies(query, "es-MX").await().indefinitely();
    }
}
