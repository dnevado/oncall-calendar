package es.medoncalls.guardias.contract;

import static es.medoncalls.guardias.contract.OpenApiContract.cumple;
import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;

/** `POST /cambios` (CESION) y `GET …/receptores-candidatos?tipo=CESION` frente al contrato (T033). */
class CambiosContractTest extends IntegracionBase {

    private static final LocalDate DIA_10 = LocalDate.of(2030, 2, 10);

    private long servicio;
    private long ana;
    private long bruno;
    private long guardiaAna;
    private MockHttpSession sesion;

    @BeforeEach
    void calendario() throws Exception {
        servicio = datos.servicio("Cardiología");
        long admin = datos.adjunto("Admin");
        ana = datos.adjunto("Ana");
        bruno = datos.adjunto("Bruno");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, ana);
        datos.miembro(servicio, bruno);
        guardiaAna = datos.guardia(ana, DIA_10, TipoGuardia.PRESENCIA, true, servicio);
        sesion = login(admin);
    }

    private org.springframework.test.web.servlet.ResultActions registrar(String json) throws Exception {
        return mvc.perform(post("/api/v1/servicios/{id}/cambios", servicio).with(csrfSpa()).session(sesion)
                .header("Idempotency-Key", "contrato-" + json.hashCode())
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void cesion201() throws Exception {
        registrar("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + guardiaAna + ",\"cedenteId\":" + ana
                + ",\"receptorId\":" + bruno + "}")
                .andExpect(status().isCreated())
                .andExpect(cumple());
    }

    @Test
    void cesion422ConTodasLasInfracciones() throws Exception {
        datos.vacaciones(bruno, DIA_10);
        datos.guardia(bruno, DIA_10, TipoGuardia.LOCALIZADA, false, servicio);
        registrar("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + guardiaAna + ",\"cedenteId\":" + ana
                + ",\"receptorId\":" + bruno + "}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(cumple())
                .andExpect(jsonPath("$.infracciones.length()").value(2))
                .andExpect(jsonPath("$.infracciones[0].mensaje").isNotEmpty());
    }

    @Test
    void cesionConGuardiaDestinoEs400() throws Exception {
        registrar("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + guardiaAna + ",\"cedenteId\":" + ana
                + ",\"receptorId\":" + bruno + ",\"guardiaDestinoId\":" + guardiaAna + "}")
                .andExpect(status().isBadRequest())
                .andExpect(cumple());
    }

    @Test
    void cuerpoIncompletoEs400() throws Exception {
        registrar("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + guardiaAna + "}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void receptoresCandidatosDeUnaCesion() throws Exception {
        long rita = datos.residente("Rita", "R2");
        datos.miembro(servicio, rita);
        mvc.perform(get("/api/v1/servicios/{s}/guardias/{g}/receptores-candidatos", servicio, guardiaAna)
                        .param("tipo", "CESION").session(sesion))
                .andExpect(status().isOk())
                .andExpect(cumple())
                // El administrador también es adjunto del servicio y sin guardia ese día.
                .andExpect(jsonPath("$[*].medico.nombre").value(
                        org.hamcrest.Matchers.containsInAnyOrder("Admin Prueba", "Bruno Prueba")))
                .andExpect(jsonPath("$[0].guardiaDestinoId").doesNotExist());
    }

    @Test
    void receptoresCandidatosDeGuardiaInexistenteEs404() throws Exception {
        mvc.perform(get("/api/v1/servicios/{s}/guardias/{g}/receptores-candidatos", servicio, 999999)
                        .param("tipo", "CESION").session(sesion))
                .andExpect(status().isNotFound())
                .andExpect(cumple());
    }
}
