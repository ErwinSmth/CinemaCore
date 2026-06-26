package pe.edu.utp.cinestar.movie_service.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import pe.edu.utp.cinestar.movie_service.model.RestriccionEdad;

@ApplicationScoped
public class RestriccionEdadRepository implements PanacheRepositoryBase<RestriccionEdad, Integer> {
    public RestriccionEdad findByCodigo(String codigo) {
        return find("codigo", codigo).firstResult();
    }
}
