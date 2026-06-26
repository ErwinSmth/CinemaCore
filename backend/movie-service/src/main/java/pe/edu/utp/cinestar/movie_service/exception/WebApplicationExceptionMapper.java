package pe.edu.utp.cinestar.movie_service.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(WebApplicationException e) {
        Map<String, Object> response = new LinkedHashMap<>();
        int status = e.getResponse().getStatus();
        
        response.put("timestamp", Instant.now().toString());
        response.put("status", status);
        response.put("error", e.getResponse().getStatusInfo().getReasonPhrase());
        response.put("message", e.getMessage() != null ? e.getMessage() : "Ocurrió un error en la capa HTTP");
        response.put("path", uriInfo.getPath());

        return Response.status(status)
                .entity(response)
                .build();
    }
}
