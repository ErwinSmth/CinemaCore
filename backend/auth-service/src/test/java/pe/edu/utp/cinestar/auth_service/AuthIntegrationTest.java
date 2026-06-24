package pe.edu.utp.cinestar.auth_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.cinestar.auth_service.model.dto.LoginRequest;
import pe.edu.utp.cinestar.auth_service.model.dto.RegisterRequest;
import pe.edu.utp.cinestar.auth_service.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Limpiamos la base de datos antes de cada prueba para mantener la idempotencia
        userRepository.deleteAll();
    }

    @Test
    void testRegistroExitoso() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@cinestar.com")
                .contrasena("Secure123*")
                .nombres("Juan")
                .apellidos("Perez")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testRegistroEmailDuplicado() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("duplicado@cinestar.com")
                .contrasena("Secure123*")
                .nombres("Juan")
                .apellidos("Perez")
                .build();

        // Primer registro exitoso
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segundo registro debe fallar con 400 Bad Request
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegistroValidacionesFormato() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("correo-invalido") // Sin @
                .contrasena("123") // Muy corta
                .nombres("") // Vacio
                .apellidos("") // Vacio
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginExitoso() throws Exception {
        // Registramos primero al usuario
        RegisterRequest regReq = RegisterRequest.builder()
                .email("login@cinestar.com")
                .contrasena("Secure123*")
                .nombres("Login")
                .apellidos("User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated());

        // Hacemos login
        LoginRequest loginReq = LoginRequest.builder()
                .email("login@cinestar.com")
                .contrasena("Secure123*")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testLoginContrasenaIncorrecta() throws Exception {
        // Registramos primero
        RegisterRequest regReq = RegisterRequest.builder()
                .email("wrongpass@cinestar.com")
                .contrasena("Secure123*")
                .nombres("Wrong")
                .apellidos("Pass")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated());

        // Hacemos login con mala contraseña
        LoginRequest loginReq = LoginRequest.builder()
                .email("wrongpass@cinestar.com")
                .contrasena("BadPass123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }
}
