package pe.edu.utp.cinestar.movie_service.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "peliculas")
public class Movie extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(name = "tmdb_id", unique = true, nullable = false)
    public Integer tmdbId;

    @Column(name = "titulo", nullable = false)
    public String titulo;

    @Column(name = "sinopsis", columnDefinition = "TEXT")
    public String sinopsis;

    @Column(name = "poster_path")
    public String posterPath;

    @Column(name = "backdrop_path")
    public String backdropPath;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "generos", nullable = false, columnDefinition = "jsonb")
    public String generos = "[]";

    @Column(name = "duracion_minutos")
    public Integer duracionMinutos;

    @Column(name = "restriccion_edad", nullable = false, length = 5)
    public String restriccionEdad = "A";

    @Column(name = "estado", nullable = false, length = 20)
    public String estado = "INACTIVO";

    @Column(name = "fecha_lanzamiento")
    public LocalDate fechaLanzamiento;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // Métodos útiles de Panache (Active Record)
    public static Movie findByTmdbId(Integer tmdbId) {
        return find("tmdbId", tmdbId).firstResult();
    }
}
