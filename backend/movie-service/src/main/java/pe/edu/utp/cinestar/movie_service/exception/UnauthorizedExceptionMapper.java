package pe.edu.utp.cinestar.movie_service.exception;

import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(UnauthorizedException e) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", 401);
        response.put("error", "Unauthorized");
        response.put("message", "Token JWT ausente, inválido o expirado. Autenticación requerida.");
        response.put("path", uriInfo.getPath());

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(response)
                .build();
    }
}
