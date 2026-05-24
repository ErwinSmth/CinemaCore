package pe.edu.utp.cinestar.auth_service.exception;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//DTO Estandar para la respuesta en caso de errores en formato JSON
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    // fecha y hora que surgio el error
    private LocalDateTime timestamp;
    // Codigo HTTP
    private int status;
    // Descripcion tipo de error HTTP
    private String error;
    // Mensaje descriptivo para el front
    private String message;
    // Endpoint donde surgio el error
    private String path;
}
