package pe.edu.utp.cinestar.movie_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.utp.cinestar.movie_service.model.Movie;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByTmdbId(Integer tmdbId);

    @Query("SELECT m FROM Movie m WHERE " +
           "(:status IS NULL OR m.estado = :status) AND " +
           "(:search IS NULL OR LOWER(m.titulo) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Movie> findByStatusAndSearch(@Param("status") String status, @Param("search") String search);

    @Query(value = "SELECT * FROM peliculas WHERE estado = 'CARTELERA' " +
           "AND (:search IS NULL OR titulo ILIKE CONCAT('%', :search, '%')) " +
           "AND (:genreJson IS NULL OR metadata @> CAST(CAST(:genreJson AS TEXT) AS jsonb))",
           nativeQuery = true)
    List<Movie> findCartelera(@Param("search") String search, @Param("genreJson") String genreJson);
}
