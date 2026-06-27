package pe.edu.utp.cinestar.movie_service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WiremockTmdbResource implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();

        // Configurar Mock para Búsqueda (Scenario 1)
        wireMockServer.stubFor(get(urlPathEqualTo("/search/movie"))
                .withQueryParam("query", matching(".*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"results\": [{\"id\": 12345, \"title\": \"Inception\"}]}")));

        // Configurar Mock para Fallo 500 (Scenario 2)
        wireMockServer.stubFor(get(urlPathEqualTo("/movie/999"))
                .willReturn(aResponse()
                        .withStatus(500)));

        return Collections.singletonMap("quarkus.rest-client.tmdb-api.url", "http://localhost:8089");
    }

    @Override
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
