package pe.edu.utp.cinestar.movie_service.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import pe.edu.utp.cinestar.movie_service.model.Movie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MovieRepository implements PanacheRepositoryBase<Movie, Long> {

    public Movie findByTmdbId(Integer tmdbId) {
        return find("tmdbId", tmdbId).firstResult();
    }

    public List<Movie> findByStatusAndSearch(String status, String search) {
        StringBuilder hql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        
        if (status != null && !status.trim().isEmpty()) {
            hql.append(" and estado = :status");
            params.put("status", status.trim());
        }
        
        if (search != null && !search.trim().isEmpty()) {
            hql.append(" and titulo ilike :search");
            params.put("search", "%" + search.trim() + "%");
        }
        
        return find(hql.toString(), params).list();
    }

    public List<Movie> findCartelera(String genre, String search) {
        StringBuilder sql = new StringBuilder("SELECT * FROM peliculas WHERE estado = 'CARTELERA'");
        Map<String, Object> params = new HashMap<>();
        
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND titulo ILIKE :search");
            params.put("search", "%" + search.trim() + "%");
        }
        
        if (genre != null && !genre.trim().isEmpty()) {
            sql.append(" AND metadata @> CAST(:genreJson AS jsonb)");
            params.put("genreJson", "{\"generos\": [\"" + genre.trim() + "\"]}");
        }
        
        jakarta.persistence.Query nativeQuery = getEntityManager().createNativeQuery(sql.toString(), Movie.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            nativeQuery.setParameter(entry.getKey(), entry.getValue());
        }
        
        @SuppressWarnings("unchecked")
        List<Movie> result = nativeQuery.getResultList();
        return result;
    }
}
