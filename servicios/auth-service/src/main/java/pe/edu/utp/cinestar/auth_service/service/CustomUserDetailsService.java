package pe.edu.utp.cinestar.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.utp.cinestar.auth_service.model.entity.UserEntity;
import pe.edu.utp.cinestar.auth_service.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

// Servicio puente entre la base de datos y Spring Security
// solo se encarga de convertir la entidad usuario al formato que entiende Spring Security
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // metodo invocado automaticamente por el AuthenticationManager al hacer login
    // @return retorna en un objeto que entiende Spring Security y usa para validar
    // claves
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // buscamos al usuario en la base de datos
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // convertimos los roles a formato de Spring
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getNombre()))
                .collect(Collectors.toList());

        // retorna un objeto de Spring Security de tipo User
        // este objeto usara el AuthenticationManager para validar la clave
        // enviada en el login vs la clave encriptada en la BD
        return new User(user.getEmail(), user.getContrasena(), authorities);
    }
}
