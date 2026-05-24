package pe.edu.utp.cinestar.auth_service.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Integer RolId;

    @Column(name = "nombre", length = 50, nullable = false, unique = true)
    private String nombre;

}
