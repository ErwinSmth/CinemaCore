package pe.edu.utp.cinestar.movie_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.utp.cinestar.movie_service.model.dto.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMovieNotFoundException(MovieNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse()
                .setTimestamp(LocalDateTime.now())
                .setStatus(HttpStatus.NOT_FOUND.value())
                .setError(HttpStatus.NOT_FOUND.getReasonPhrase())
                .setMessage(ex.getMessage())
                .setPath(""); // Path se puede omitir o inyectar HttpServletRequest
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TMDBIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleTMDBIntegrationException(TMDBIntegrationException ex) {
        ErrorResponse errorResponse = new ErrorResponse()
                .setTimestamp(LocalDateTime.now())
                .setStatus(HttpStatus.BAD_GATEWAY.value())
                .setError(HttpStatus.BAD_GATEWAY.getReasonPhrase())
                .setMessage(ex.getMessage())
                .setPath("");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse()
                .setTimestamp(LocalDateTime.now())
                .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .setMessage(ex.getMessage())
                .setPath("");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
