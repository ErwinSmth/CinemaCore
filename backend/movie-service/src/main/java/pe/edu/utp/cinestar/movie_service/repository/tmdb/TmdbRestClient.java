package pe.edu.utp.cinestar.movie_service.repository.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class TmdbRestClient {

    @Value("${tmdb.api.url}")
    private String apiUrl;

    @Value("${tmdb.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate;

    public TmdbRestClient() {
        this.restTemplate = new RestTemplate();
    }

    private HttpEntity<String> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiToken);
        headers.set("accept", "application/json");
        return new HttpEntity<>(headers);
    }

    public JsonNode searchMovies(String query, String language) {
        String url = apiUrl + "/search/movie?query=" + query + "&language=" + language;
        
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), JsonNode.class);
        return response.getBody();
    }

    public JsonNode getMovieDetails(int movieId, String appendToResponse, String language) {
        String url = apiUrl + "/movie/" + movieId + "?append_to_response=" + appendToResponse + "&language=" + language;
                
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), JsonNode.class);
        return response.getBody();
    }
}
