package es.medoncalls.guardias.auth;

import java.io.IOException;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sesión en el servidor (cookie `MOC_SESSION`, ver application.yml) y CSRF con cookie legible
 * por la SPA (`XSRF-TOKEN` → cabecera `X-XSRF-TOKEN`). Todo lo que no es el login exige sesión;
 * el rol de administrador de cada servicio lo comprueba `ServicioAccess` en cada operación.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain api(HttpSecurity http, SecurityContextRepository contextos, ObjectMapper json)
            throws Exception {
        CookieCsrfTokenRepository csrfRepositorio = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepositorio.setCookieCustomizer(c -> c.path("/").secure(true).sameSite("Strict"));
        http
                .securityContext(s -> s.securityContextRepository(contextos))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(c -> c.csrfTokenRepository(csrfRepositorio).csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/logout").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> problema(res, json, HttpStatus.UNAUTHORIZED,
                                "No autenticado", "Inicia sesión para continuar."))
                        .accessDeniedHandler((req, res, ex) -> problema(res, json, HttpStatus.FORBIDDEN,
                                "Prohibido", "Petición rechazada.")))
                .logout(l -> l
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable());
        return http.build();
    }

    private static void problema(HttpServletResponse res, ObjectMapper json, HttpStatus estado, String titulo,
            String detalle) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        res.setStatus(estado.value());
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        json.writeValue(res.getOutputStream(), problema);
    }

    /**
     * Manejador de CSRF para SPA (guía de Spring Security 6): la cabecera lleva el valor en
     * claro de la cookie; los formularios, el valor protegido frente a BREACH.
     */
    static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

        private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            xor.handle(request, response, csrfToken);
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            String cabecera = request.getHeader(csrfToken.getHeaderName());
            return (StringUtils.hasText(cabecera) ? plain : xor).resolveCsrfTokenValue(request, csrfToken);
        }
    }

    /** Fuerza la emisión de la cookie `XSRF-TOKEN` en cada respuesta. */
    static final class CsrfCookieFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (token != null) {
                token.getToken();
            }
            chain.doFilter(request, response);
        }
    }
}
