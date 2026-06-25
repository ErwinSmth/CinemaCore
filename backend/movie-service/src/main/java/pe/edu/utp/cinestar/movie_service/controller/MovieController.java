package pe.edu.utp.cinestar.movie_service.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import pe.edu.utp.cinestar.movie_service.controller.api.MoviesApi;
import pe.edu.utp.cinestar.movie_service.model.dto.UpdateMovieRequest;
import pe.edu.utp.cinestar.movie_service.service.MovieService;

@ApplicationScoped
public class MovieController implements MoviesApi {

    @Inject
    MovieService movieService;

    @Override
    public Response searchTmdbMovies(String query) {
        return Response.ok(movieService.searchTmdbMovies(query)).build();
    }

    @Override
    public Response importMovie(Integer tmdbId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\": \"Pending implementation: Import Movie\"}").build();
    }

    @Override
    public Response getAllMoviesAdmin(String status, String search) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\": \"Pending implementation: Get All Admin\"}").build();
    }

    @Override
    public Response getMovieById(Integer id) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\": \"Pending implementation: Get Movie\"}").build();
    }

    @Override
    public Response updateMovie(Integer id, UpdateMovieRequest updateMovieRequest) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\": \"Pending implementation: Update Movie\"}").build();
    }

    @Override
    public Response deleteMovie(Integer id) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\": \"Pending implementation: Delete Movie\"}").build();
    }

    @Override
    public Response getCartelera(String genre, String search) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\": \"Pending implementation: Get Cartelera\"}").build();
    }
}
