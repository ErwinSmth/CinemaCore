package pe.edu.utp.cinestar.movie_service.model;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "restriccion_edad")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestriccionEdad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restriccion_id")
    public Integer id;

    @Column(name = "codigo", unique = true, nullable = false, length = 10)
    public String codigo;

    @Column(name = "descripcion", nullable = false, length = 100)
    public String descripcion;

}
