package pe.edu.utp.cinestar.auth_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Filtro de seguridad que se ejecuta una vez por cada peticion HTTP que ingresa

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // se intenta obtener el token del header de la peticion HTTP
        String token = extractTokenFromRequest(request);

        // si existe token y el jwtprovider valida que es valido
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            // se obtiene el email incrustado en el token
            String email = jwtProvider.getEmailFromToken(token);

            // se crea el objeto de autenticacion
            // esto le dice al back que el usuario esta autenticado
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null,
                    List.of()); // los roles vendran en el futuro

            // se guarda la informacion del usuario para que
            // toda la aplicacion conozca quien es el usuario
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // permite que la peticion continua a los controladores
        filterChain.doFilter(request, response);
    }

    // Helper para limpiar el encabezado HTTP y extrar solo el token
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
