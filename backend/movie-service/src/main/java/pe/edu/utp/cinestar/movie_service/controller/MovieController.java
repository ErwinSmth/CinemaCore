package pe.edu.utp.cinestar.movie_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.cinestar.movie_service.model.dto.MovieAdminResponse;
import pe.edu.utp.cinestar.movie_service.model.dto.MovieCarteleraResponse;
import pe.edu.utp.cinestar.movie_service.model.dto.MovieDetailResponse;
import pe.edu.utp.cinestar.movie_service.model.dto.UpdateMovieRequest;
import pe.edu.utp.cinestar.movie_service.model.Movie;
import pe.edu.utp.cinestar.movie_service.service.MovieService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping(value = "/tmdb/search", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<String> searchTmdbMovies(@RequestParam("query") String query) {
        return ResponseEntity.ok(movieService.searchTmdbMovies(query).toString());
    }

    @PostMapping("/tmdb/import/{tmdbId}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<String> importMovie(@PathVariable("tmdbId") Integer tmdbId) {
        Long idLocal = movieService.importMovie(tmdbId);
        return ResponseEntity.created(URI.create("/movies/" + idLocal)).body("{\"id\": " + idLocal + "}");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<List<MovieAdminResponse>> getAllMoviesAdmin(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(movieService.getAllMoviesAdmin(status, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailResponse> getMovieById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<Movie> updateMovie(@PathVariable("id") Long id, @RequestBody UpdateMovieRequest updateMovieRequest) {
        return ResponseEntity.ok(movieService.updateMovie(id, updateMovieRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> deleteMovie(@PathVariable("id") Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MovieCarteleraResponse>> getCartelera(
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(movieService.getCartelera(genre, search));
    }

    @GetMapping("/pre-estreno")
    public ResponseEntity<List<MovieCarteleraResponse>> getPreEstrenos(
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(movieService.getPreEstrenos(genre, search));
    }
}
