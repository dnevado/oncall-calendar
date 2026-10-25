package es.medoncalls.guardias.integration;

import static es.medoncalls.guardias.contract.OpenApiContract.cumple;
import static org.assertj.core.api.Assertions.assertThat;
import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

import es.medoncalls.guardias.auth.LegacyPasswordVerifier;
import jakarta.servlet.http.Cookie;
import es.medoncalls.guardias.support.DatosPrueba;
import es.medoncalls.guardias.support.IntegracionBase;

class AuthIT extends IntegracionBase {

    @Autowired
    private ServerProperties servidor;

    private long servicio;
    private long otroServicio;
    private long admin;
    private long medico;

    @BeforeEach
    void datos() {
        servicio = datos.servicio("Cardiología");
        otroServicio = datos.servicio("Neumología");
        admin = datos.adjunto("Ana");
        medico = datos.adjunto("Bruno");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, medico);
    }

    private ResultActions intentarLogin(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").with(csrfSpa())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    @Test
    void loginCorrectoDevuelveElUsuarioYSusServicios() throws Exception {
        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD)
                .andExpect(status().isOk())
                .andExpect(cumple())
                .andExpect(jsonPath("$.id").value(admin))
                .andExpect(jsonPath("$.nombre").value("Ana Prueba"))
                .andExpect(jsonPath("$.serviciosAdministrados[0].id").value(servicio));
    }

    @Test
    void contraseñaIncorrectaYCuentaInexistenteDanElMismo401() throws Exception {
        String incorrecta = intentarLogin(datos.email(admin), "otra").andExpect(status().isUnauthorized())
                .andExpect(cumple()).andReturn().getResponse().getContentAsString();
        String inexistente = intentarLogin("nadie@ejemplo.test", "otra").andExpect(status().isUnauthorized())
                .andExpect(cumple()).andReturn().getResponse().getContentAsString();
        assertThat(inexistente).isEqualTo(incorrecta);
    }

    @Test
    void medicoInactivoONoConfirmadoNoEntra() throws Exception {
        jdbc.update("UPDATE medicos SET Activo = 0 WHERE ID = ?", admin);
        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD).andExpect(status().isUnauthorized());

        jdbc.update("UPDATE medicos SET Confirmado = 0 WHERE ID = ?", medico);
        intentarLogin(datos.email(medico), DatosPrueba.PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    void primerLoginGuardaHashArgon2YElLegacyNoSeToca() throws Exception {
        String legacyAntes = jdbc.queryForObject("SELECT Password FROM medicos WHERE ID = ?", String.class, admin);
        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD).andExpect(status().isOk());

        assertThat(jdbc.queryForObject("SELECT PasswordHashV2 FROM medicos WHERE ID = ?", String.class, admin))
                .startsWith("$argon2id$");
        assertThat(jdbc.queryForObject("SELECT Password FROM medicos WHERE ID = ?", String.class, admin))
                .isEqualTo(legacyAntes);
        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD).andExpect(status().isOk());
    }

    @Test
    void siElLegacyCambiaLaContraseñaSeDescartaElHashNuevo() throws Exception {
        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD).andExpect(status().isOk());
        jdbc.update("UPDATE medicos SET Password = ? WHERE ID = ?",
                new LegacyPasswordVerifier().hashLegacy("nueva-clave"), admin);

        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD).andExpect(status().isUnauthorized());
        intentarLogin(datos.email(admin), "nueva-clave").andExpect(status().isOk());
    }

    @Test
    void cincoFallosBloqueanQuinceMinutosInclusoConLaContraseñaCorrecta() throws Exception {
        for (int i = 0; i < 5; i++) {
            intentarLogin(datos.email(admin), "mala").andExpect(status().isUnauthorized());
        }
        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD).andExpect(status().isUnauthorized());

        reloj.avanzar(Duration.ofMinutes(14));
        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD).andExpect(status().isUnauthorized());

        reloj.avanzar(Duration.ofMinutes(1));
        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD).andExpect(status().isOk());
    }

    @Test
    void unLoginCorrectoReiniciaElContador() throws Exception {
        for (int i = 0; i < 4; i++) {
            intentarLogin(datos.email(admin), "mala").andExpect(status().isUnauthorized());
        }
        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD).andExpect(status().isOk());
        intentarLogin(datos.email(admin), "mala").andExpect(status().isUnauthorized());
        intentarLogin(datos.email(admin), DatosPrueba.PASSWORD).andExpect(status().isOk());
    }

    @Test
    void sinCabeceraCsrfOConOtroValorSeRechazaConSesion() throws Exception {
        MockHttpSession sesion = login(admin);
        String cuerpo = "{\"tipo\":\"CESION\",\"guardiaOrigenId\":1,\"cedenteId\":1,\"receptorId\":2}";
        mvc.perform(post("/api/v1/servicios/{id}/cambios", servicio).session(sesion)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/servicios/{id}/cambios", servicio).session(sesion)
                        .cookie(new Cookie("XSRF-TOKEN", "uno")).header("X-XSRF-TOKEN", "otro")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());
        assertThat(datos.contar("guardias_cambios")).isZero();
    }

    @Test
    void cookiesDeSesionYCsrf() throws Exception {
        // La cookie de sesión la emite el contenedor de servlets con esta configuración.
        var cookieSesion = servidor.getServlet().getSession().getCookie();
        assertThat(cookieSesion.getName()).isEqualTo("MOC_SESSION");
        assertThat(cookieSesion.getHttpOnly()).isTrue();
        assertThat(cookieSesion.getSecure()).isTrue();
        assertThat(cookieSesion.getSameSite()).isEqualTo(SameSite.STRICT);

        // La de CSRF la lee la SPA: no es HttpOnly, pero sí Secure y SameSite=Strict.
        // Se mira el objeto Cookie: el Set-Cookie que compone la respuesta simulada omite SameSite.
        Cookie csrf = mvc.perform(get("/api/v1/auth/me")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrf).isNotNull();
        assertThat(csrf.getSecure()).isTrue();
        assertThat(csrf.isHttpOnly()).isFalse();
        assertThat(csrf.getPath()).isEqualTo("/");
        assertThat(csrf.getAttribute("SameSite")).isEqualTo("Strict");
    }

    @Test
    void unaPeticionAnonimaNoCreaSesion() throws Exception {
        assertThat(mvc.perform(get("/api/v1/servicios/{id}/guardias", servicio).param("desde", "2030-01-01")
                .param("hasta", "2030-01-31")).andExpect(status().isUnauthorized())
                .andReturn().getRequest().getSession(false)).isNull();
    }

    @Test
    void sinSesionSeResponde401() throws Exception {
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized()).andExpect(cumple());
        mvc.perform(get("/api/v1/servicios/{id}/guardias", servicio).param("desde", "2030-01-01")
                .param("hasta", "2030-01-31")).andExpect(status().isUnauthorized()).andExpect(cumple());
    }

    @Test
    void meYLogout() throws Exception {
        MockHttpSession sesion = login(admin);
        mvc.perform(get("/api/v1/auth/me").session(sesion))
                .andExpect(status().isOk()).andExpect(cumple())
                .andExpect(jsonPath("$.id").value(admin));

        mvc.perform(post("/api/v1/auth/logout").with(csrfSpa()).session(sesion))
                .andExpect(status().isNoContent()).andExpect(cumple());
        mvc.perform(get("/api/v1/auth/me").session(sesion)).andExpect(status().isUnauthorized());
    }

    @Test
    void soloElAdministradorDelServicioVeSuCalendario() throws Exception {
        long adminOtro = datos.adjunto("Carla");
        datos.miembro(otroServicio, adminOtro, true, true);
        datos.miembro(servicio, adminOtro);

        mvc.perform(get("/api/v1/servicios/{id}/guardias", servicio).session(login(medico))
                .param("desde", "2030-01-01").param("hasta", "2030-01-31"))
                .andExpect(status().isForbidden()).andExpect(cumple());
        mvc.perform(get("/api/v1/servicios/{id}/guardias", servicio).session(login(adminOtro))
                .param("desde", "2030-01-01").param("hasta", "2030-01-31"))
                .andExpect(status().isForbidden()).andExpect(cumple());
        mvc.perform(get("/api/v1/servicios/{id}/guardias", servicio).session(login(admin))
                .param("desde", "2030-01-01").param("hasta", "2030-01-31"))
                .andExpect(status().isOk()).andExpect(cumple());
    }

    @Test
    void revocarElRolSurteEfectoSinCerrarSesion() throws Exception {
        MockHttpSession sesion = login(admin);
        jdbc.update("UPDATE guardias_servicios_medicos SET IsAdministrator = 0 WHERE IdMedico = ?", admin);
        mvc.perform(get("/api/v1/servicios/{id}/guardias", servicio).session(sesion)
                .param("desde", "2030-01-01").param("hasta", "2030-01-31"))
                .andExpect(status().isForbidden());
    }
}
