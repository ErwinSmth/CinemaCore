package pe.edu.utp.cinestar.movie_service.model;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "peliculas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "peliculas_id")
    public Long id;

    @Column(name = "tmdb_id", unique = true, nullable = false)
    public Integer tmdbId;

    @Column(name = "titulo", nullable = false)
    public String titulo;

    @Column(name = "sinopsis", columnDefinition = "TEXT")
    public String sinopsis;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    public com.fasterxml.jackson.databind.JsonNode metadata;

    @Column(name = "duracion_min")
    public Integer duracionMin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "restriccion_id", nullable = false)
    public RestriccionEdad restriccionEdad;

    @Column(name = "estado", nullable = false, length = 20)
    public String estado = "INACTIVO";

    @Column(name = "fecha_estreno")
    public LocalDate fechaEstreno;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

}
