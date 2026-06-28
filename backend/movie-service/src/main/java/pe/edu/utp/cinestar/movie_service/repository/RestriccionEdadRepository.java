package pe.edu.utp.cinestar.movie_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.cinestar.movie_service.model.RestriccionEdad;

import java.util.Optional;

@Repository
public interface RestriccionEdadRepository extends JpaRepository<RestriccionEdad, Integer> {
    Optional<RestriccionEdad> findByCodigo(String codigo);
}
