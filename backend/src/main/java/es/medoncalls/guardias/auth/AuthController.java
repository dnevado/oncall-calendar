package es.medoncalls.guardias.auth;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/** `POST /auth/login`, `GET /auth/me`. El logout lo atiende la configuración de seguridad. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final MedicoAuthMapper mapper;
    private final SecurityContextRepository contextos;

    public AuthController(AuthService authService, MedicoAuthMapper mapper, SecurityContextRepository contextos) {
        this.authService = authService;
        this.mapper = mapper;
        this.contextos = contextos;
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {
    }

    public record UsuarioDto(long id, String nombre, List<ServicioResumen> serviciosAdministrados) {
    }

    @PostMapping("/login")
    public UsuarioDto login(@Valid @RequestBody LoginRequest peticion, HttpServletRequest request,
            HttpServletResponse response) {
        UsuarioSesion usuario = authService.login(peticion.email(), peticion.password());

        // Sesión nueva tras autenticar (fijación de sesión).
        request.getSession(true);
        request.changeSessionId();
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(usuario, null, List.of()));
        SecurityContextHolder.setContext(contexto);
        contextos.saveContext(contexto, request, response);
        return dto(usuario);
    }

    @GetMapping("/me")
    public UsuarioDto me(@AuthenticationPrincipal UsuarioSesion usuario) {
        return dto(usuario);
    }

    private UsuarioDto dto(UsuarioSesion usuario) {
        return new UsuarioDto(usuario.medicoId(), usuario.nombre(),
                mapper.selectServiciosAdministrados(usuario.medicoId()));
    }
}
