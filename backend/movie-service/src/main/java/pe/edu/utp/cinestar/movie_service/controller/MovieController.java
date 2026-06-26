package pe.edu.utp.cinestar.movie_service.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import pe.edu.utp.cinestar.movie_service.controller.api.MoviesApi;
import pe.edu.utp.cinestar.movie_service.model.dto.UpdateMovieRequest;
import pe.edu.utp.cinestar.movie_service.service.MovieService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import java.net.URI;

@ApplicationScoped
public class MovieController implements MoviesApi {

    @Inject
    MovieService movieService;

    @Override
    @RolesAllowed("ROLE_ADMINISTRADOR")
    public Response searchTmdbMovies(String query) {
        return Response.ok(movieService.searchTmdbMovies(query)).build();
    }

    @Override
    @RolesAllowed("ROLE_ADMINISTRADOR")
    public Response importMovie(Integer tmdbId) {
        Long idLocal = movieService.importMovie(tmdbId);
        return Response.created(URI.create("/movies/" + idLocal)).entity("{\"id\": " + idLocal + "}").build();
    }

    @Override
    @RolesAllowed("ROLE_ADMINISTRADOR")
    public Response getAllMoviesAdmin(String status, String search) {
        return Response.ok(movieService.getAllMoviesAdmin(status, search)).build();
    }

    @Override
    @PermitAll
    public Response getMovieById(Long id) {
        return Response.ok(movieService.getMovieById(id)).build();
    }

    @Override
    @RolesAllowed("ROLE_ADMINISTRADOR")
    public Response updateMovie(Long id, UpdateMovieRequest updateMovieRequest) {
        return Response.ok(movieService.updateMovie(id, updateMovieRequest)).build();
    }

    @Override
    @RolesAllowed("ROLE_ADMINISTRADOR")
    public Response deleteMovie(Long id) {
        movieService.deleteMovie(id);
        return Response.noContent().build();
    }

    @Override
    @PermitAll
    public Response getCartelera(String genre, String search) {
        return Response.ok(movieService.getCartelera(genre, search)).build();
    }
}
