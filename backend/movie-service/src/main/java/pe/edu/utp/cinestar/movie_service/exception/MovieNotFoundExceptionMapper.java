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
public class MovieNotFoundExceptionMapper implements ExceptionMapper<MovieNotFoundException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(MovieNotFoundException e) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", 404);
        response.put("error", "No Encontrado");
        response.put("message", e.getMessage());
        response.put("path", uriInfo.getPath());

        return Response.status(Response.Status.NOT_FOUND)
                .entity(response)
                .build();
    }
}
