package pe.edu.utp.cinestar.movie_service.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class TmdbWiremockResource implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();

        // Configurar stubs para TMDB
        // 1. Búsqueda de películas
        wireMockServer.stubFor(get(urlPathEqualTo("/search/movie"))
                .withQueryParam("query", matching(".*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"results\": [{\"id\": 550, \"title\": \"Fight Club\"}]}")));

        // 2. Detalle de película
        wireMockServer.stubFor(get(urlPathMatching("/movie/[0-9]+"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 550, \"title\": \"Fight Club\", \"overview\": \"A ticking-time-bomb insomniac...\"}")));

        // Inyectar la URL dinámica en Quarkus para el TmdbRestClient
        return Map.of("quarkus.rest-client.tmdb-api.url", wireMockServer.baseUrl());
    }

    @Override
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
