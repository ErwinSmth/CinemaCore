package pe.edu.utp.cinestar.movie_service.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class TMDBIntegrationExceptionMapper implements ExceptionMapper<TMDBIntegrationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(TMDBIntegrationException e) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", 502);
        response.put("error", "Error de Integración con TMDB");
        response.put("message", e.getMessage());
        response.put("path", uriInfo.getPath());

        return Response.status(Response.Status.BAD_GATEWAY)
                .entity(response)
                .build();
    }
}
