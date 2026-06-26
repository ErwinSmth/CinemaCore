package pe.edu.utp.cinestar.movie_service.exception;

import io.quarkus.security.ForbiddenException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ForbiddenException e) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", 403);
        response.put("error", "Forbidden");
        response.put("message", "Acceso denegado. No tienes los roles necesarios (ej. ROLE_ADMINISTRADOR) para realizar esta acción.");
        response.put("path", uriInfo.getPath());

        return Response.status(Response.Status.FORBIDDEN)
                .entity(response)
                .build();
    }
}
