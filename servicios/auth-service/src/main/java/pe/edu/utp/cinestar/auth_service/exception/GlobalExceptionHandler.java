package pe.edu.utp.cinestar.auth_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Interceptador de excepciones a nivel de controlador
// formatea las respuestas en un JSON limpio
@RestControllerAdvice // le dico a Spring que esta clase maneja las excepciones de todos los
// controladores
public class GlobalExceptionHandler {

        // Captura errores de validacion de los DTOS (@Valid, @NotBlank, etc)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                // Obtiene el resultado de la validacion y muestra el primer error
                String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                                .map(FieldError::getDefaultMessage)
                                .findFirst()
                                .orElse("Error de validación");

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(errorMessage)
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Intercepta las excepciones de la capa de servicio
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequestException(
                        BadRequestException ex, HttpServletRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Captura credenciales incorrectas en el login (contraseña erronea)
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(
                        BadCredentialsException ex, HttpServletRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                                .message("Credenciales inválidas")
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Captura cuando el email no existe en la base de datos
        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
                        UsernameNotFoundException ex, HttpServletRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.NOT_FOUND.value())
                                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                                .message("Usuario no encontrado")
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // Captura cualquier otra excepcion no manejada
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                                .message("Error interno del servidor")
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
