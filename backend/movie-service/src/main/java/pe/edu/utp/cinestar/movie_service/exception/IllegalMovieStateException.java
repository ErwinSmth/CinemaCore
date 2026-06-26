package pe.edu.utp.cinestar.movie_service.exception;

public class IllegalMovieStateException extends RuntimeException {
    public IllegalMovieStateException(String message) {
        super(message);
    }
}
