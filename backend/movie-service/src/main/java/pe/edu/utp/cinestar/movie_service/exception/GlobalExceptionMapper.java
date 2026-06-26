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
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception e) {
        // En caso de que se lance otra excepción controlada (ej. NotAuthorized)
        // Quarkus suele interceptarla antes si tiene su propio mapper.
        // Esto captura los 500 puros.
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", 500);
        response.put("error", "Error Interno del Servidor");
        response.put("message", "Se produjo un error inesperado al procesar la solicitud.");
        response.put("path", uriInfo.getPath());

        // Imprimimos el log para auditoría, ya que lo ocultamos al cliente
        e.printStackTrace();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(response)
                .build();
    }
}
