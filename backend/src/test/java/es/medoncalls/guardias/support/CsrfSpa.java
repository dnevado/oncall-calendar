package es.medoncalls.guardias.support;

import java.util.UUID;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jakarta.servlet.http.Cookie;

/**
 * CSRF como lo hace la SPA: devuelve en la cabecera `X-XSRF-TOKEN` el valor en claro de la cookie
 * `XSRF-TOKEN` (ver `SecurityConfig`).
 */
public final class CsrfSpa {

    private CsrfSpa() {
    }

    public static RequestPostProcessor csrfSpa() {
        return request -> {
            String token = UUID.randomUUID().toString();
            request.setCookies(new Cookie("XSRF-TOKEN", token));
            request.addHeader("X-XSRF-TOKEN", token);
            return request;
        };
    }
}
