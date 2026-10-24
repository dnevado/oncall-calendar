package es.medoncalls.guardias.integration;

import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;

/** Historia 3, escenarios 1–4 con CESION y CAMBIO, en la misma transacción que el cambio (T053). */
class AjusteResidentesIT extends IntegracionBase {

    private static final LocalDate DIA_10 = LocalDate.of(2030, 2, 10);
    private static final LocalDate DIA_15 = LocalDate.of(2030, 2, 15);

    private long servicio;
    private long ana;
    private long r1;
    private long r2;
    private long r3;
    private long r4;
    private MockHttpSession sesion;

    @BeforeEach
    void plantilla() throws Exception {
        servicio = datos.servicio("Cardiología");
        long admin = datos.adjunto("Admin");
        ana = datos.adjunto("Ana");
        r1 = datos.residente("Raquel", "R1");
        r2 = datos.residente("Rita", "R2");
        r3 = datos.residente("Rosa", "R3");
        r4 = datos.residente("Raúl", "R4");
        datos.miembro(servicio, admin, true, true);
        for (long medico : new long[] {ana, r1, r2, r3, r4}) {
            datos.miembro(servicio, medico);
        }
        sesion = login(admin);
    }

    @AfterEach
    void quitarFallo() {
        jdbc.execute("DROP TRIGGER IF EXISTS prueba_falla_alta_cambio");
    }

    private ResultActions ceder(long guardia, long cedente, long receptor) throws Exception {
        return mvc.perform(post("/api/v1/servicios/{id}/cambios", servicio).with(csrfSpa()).session(sesion)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + guardia + ",\"cedenteId\":" + cedente
                        + ",\"receptorId\":" + receptor + "}"));
    }

    private String tipo(long guardia) {
        return jdbc.queryForObject("SELECT Tipo FROM guardias_medicos WHERE Id = ?", String.class, guardia);
    }

    @Test
    void escenario1_entraUnR4_elRefuerzoPasaALocalizada() throws Exception {
        long residente = datos.guardia(r2, DIA_10, TipoGuardia.RESIDENTE, false, servicio);
        long refuerzo = datos.guardia(ana, DIA_10, TipoGuardia.REFUERZO, false, servicio);

        ceder(residente, r2, r4).andExpect(status().isCreated());

        assertThat(tipo(refuerzo)).isEqualTo("localizada");
        assertThat(tipo(residente)).isEmpty();
    }

    @Test
    void escenario2_saleElR3_laLocalizadaPasaARefuerzo() throws Exception {
        long residente = datos.guardia(r3, DIA_10, TipoGuardia.RESIDENTE, false, servicio);
        long localizada = datos.guardia(ana, DIA_10, TipoGuardia.LOCALIZADA, false, servicio);

        ceder(residente, r3, r1).andExpect(status().isCreated());

        assertThat(tipo(localizada)).isEqualTo("refuerzo");
    }

    @Test
    void escenario3_ambosR3aR5_oNinguno_sinCambios() throws Exception {
        long deR3 = datos.guardia(r3, DIA_10, TipoGuardia.RESIDENTE, false, servicio);
        long localizada = datos.guardia(ana, DIA_10, TipoGuardia.LOCALIZADA, false, servicio);
        ceder(deR3, r3, r4).andExpect(status().isCreated());
        assertThat(tipo(localizada)).isEqualTo("localizada");

        long deR1 = datos.guardia(r1, DIA_15, TipoGuardia.RESIDENTE, false, servicio);
        long refuerzo = datos.guardia(ana, DIA_15, TipoGuardia.REFUERZO, false, servicio);
        ceder(deR1, r1, r2).andExpect(status().isCreated());
        assertThat(tipo(refuerzo)).isEqualTo("refuerzo");
    }

    @Test
    void escenario4_tambienCuandoElR3aR5EsQuienEntrega() throws Exception {
        // El R4 cede (es el solicitante) y el día se queda sin R3–R5.
        long deR4 = datos.guardia(r4, DIA_10, TipoGuardia.RESIDENTE, false, servicio);
        long localizada = datos.guardia(ana, DIA_10, TipoGuardia.LOCALIZADA, false, servicio);

        ceder(deR4, r4, r2).andExpect(status().isCreated());

        assertThat(tipo(localizada)).isEqualTo("refuerzo");
    }

    @Test
    void intercambio_ajustaLasDosFechas() throws Exception {
        long r4En10 = datos.guardia(r4, DIA_10, TipoGuardia.RESIDENTE, false, servicio);
        long localizada10 = datos.guardia(ana, DIA_10, TipoGuardia.LOCALIZADA, false, servicio);
        long r2En15 = datos.guardia(r2, DIA_15, TipoGuardia.RESIDENTE, false, servicio);
        long refuerzo15 = datos.guardia(ana, DIA_15, TipoGuardia.REFUERZO, false, servicio);

        mvc.perform(post("/api/v1/servicios/{id}/cambios", servicio).with(csrfSpa()).session(sesion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"CAMBIO\",\"guardiaOrigenId\":" + r4En10 + ",\"cedenteId\":" + r4
                                + ",\"receptorId\":" + r2 + ",\"guardiaDestinoId\":" + r2En15 + "}"))
                .andExpect(status().isCreated());

        assertThat(tipo(localizada10)).isEqualTo("refuerzo");
        assertThat(tipo(refuerzo15)).isEqualTo("localizada");
    }

    @Test
    void soloSeAjustanLasGuardiasDelMismoServicio() throws Exception {
        long otroServicio = datos.servicio("Neumología");
        long residente = datos.guardia(r2, DIA_10, TipoGuardia.RESIDENTE, false, servicio);
        long refuerzoAqui = datos.guardia(ana, DIA_10, TipoGuardia.REFUERZO, false, servicio);
        long refuerzoOtro = datos.guardia(ana, DIA_10, TipoGuardia.REFUERZO, false, otroServicio);

        ceder(residente, r2, r4).andExpect(status().isCreated());

        assertThat(tipo(refuerzoAqui)).isEqualTo("localizada");
        assertThat(tipo(refuerzoOtro)).isEqualTo("refuerzo");
    }

    @Test
    void atomicidad_unFalloAlRegistrarDeshaceTambienElAjuste() throws Exception {
        long residente = datos.guardia(r2, DIA_10, TipoGuardia.RESIDENTE, false, servicio);
        long refuerzo = datos.guardia(ana, DIA_10, TipoGuardia.REFUERZO, false, servicio);
        jdbc.execute("CREATE TRIGGER prueba_falla_alta_cambio BEFORE INSERT ON guardias_cambios FOR EACH ROW"
                + " SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'fallo inyectado'");

        ceder(residente, r2, r4).andExpect(status().is5xxServerError());

        assertThat(tipo(refuerzo)).isEqualTo("refuerzo");
        assertThat(datos.medicoDeGuardia(residente)).isEqualTo(r2);
        assertThat(datos.contar("guardias_cambios")).isZero();
    }
}
