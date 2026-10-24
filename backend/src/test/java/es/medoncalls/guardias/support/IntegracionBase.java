package es.medoncalls.guardias.support;

import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base de las pruebas de integración: contexto completo contra MySQL 5.7 (Testcontainers), con
 * datos ficticios limpios en cada prueba y «hoy» fijado en {@link #HOY}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegracionBase.Reloj.class)
public abstract class IntegracionBase {

    public static final LocalDate HOY = LocalDate.of(2030, 1, 15);

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registro) {
        MySqlLegacyContainer.registrar(registro);
    }

    @TestConfiguration
    static class Reloj {
        @Bean
        @Primary
        RelojPrueba relojPrueba() {
            return new RelojPrueba(HOY);
        }
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected RelojPrueba reloj;

    @Autowired
    protected JdbcTemplate jdbc;

    protected DatosPrueba datos;

    @BeforeEach
    void prepararBase() {
        datos = new DatosPrueba(jdbc);
        datos.limpiar();
        reloj.fijar(HOY);
    }

    /** Inicia sesión como el médico indicado y devuelve la sesión del servidor. */
    protected MockHttpSession login(long medicoId) throws Exception {
        String cuerpo = "{\"email\":\"" + datos.email(medicoId) + "\",\"password\":\"" + DatosPrueba.PASSWORD + "\"}";
        return (MockHttpSession) mvc.perform(post("/api/v1/auth/login").with(csrfSpa())
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }
}
