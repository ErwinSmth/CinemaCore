package pe.edu.utp.cinestar.auth_service.exception;

//Exception para errores de logica de negocio, se usa en la capa de servicio
//(ej. si el correo ya existe)
public class BadRequestException extends RuntimeException {

    // Mensaje que mostrara la exception
    public BadRequestException(String message) {
        super(message);
    }
}
