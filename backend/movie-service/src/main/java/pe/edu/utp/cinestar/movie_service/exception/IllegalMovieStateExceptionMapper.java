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
public class IllegalMovieStateExceptionMapper implements ExceptionMapper<IllegalMovieStateException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(IllegalMovieStateException e) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", 409);
        response.put("error", "Estado Inválido");
        response.put("message", e.getMessage());
        response.put("path", uriInfo.getPath());

        return Response.status(Response.Status.CONFLICT)
                .entity(response)
                .build();
    }
}
