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

/** `POST /cambios` y `GET …/receptores-candidatos` (CESION y CAMBIO) frente al contrato (T033, T046). */
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
    void intercambio201() throws Exception {
        long guardiaBruno = datos.guardia(bruno, DIA_10.plusDays(5), TipoGuardia.LOCALIZADA, false, servicio);
        registrar("{\"tipo\":\"CAMBIO\",\"guardiaOrigenId\":" + guardiaAna + ",\"cedenteId\":" + ana
                + ",\"receptorId\":" + bruno + ",\"guardiaDestinoId\":" + guardiaBruno + "}")
                .andExpect(status().isCreated())
                .andExpect(cumple())
                .andExpect(jsonPath("$.fechaDestino").value("2030-02-15"))
                .andExpect(jsonPath("$.guardiaDestino.tipo").value("LOCALIZADA"));
    }

    @Test
    void intercambioSinGuardiaDestinoEs400() throws Exception {
        registrar("{\"tipo\":\"CAMBIO\",\"guardiaOrigenId\":" + guardiaAna + ",\"cedenteId\":" + ana
                + ",\"receptorId\":" + bruno + "}")
                .andExpect(status().isBadRequest())
                .andExpect(cumple());
    }

    @Test
    void receptoresCandidatosDeUnIntercambio() throws Exception {
        long guardiaBruno = datos.guardia(bruno, DIA_10.plusDays(5), TipoGuardia.LOCALIZADA, false, servicio);
        long rita = datos.residente("Rita", "R2");
        datos.miembro(servicio, rita);
        datos.guardia(rita, DIA_10.plusDays(5), TipoGuardia.RESIDENTE, false, servicio);
        mvc.perform(get("/api/v1/servicios/{s}/guardias/{g}/receptores-candidatos", servicio, guardiaAna)
                        .param("tipo", "CAMBIO").param("fechaDestino", "2030-02-15").session(sesion))
                .andExpect(status().isOk())
                .andExpect(cumple())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].medico.id").value(bruno))
                .andExpect(jsonPath("$[0].guardiaDestinoId").value(guardiaBruno));
    }

    @Test
    void receptoresCandidatosDeUnIntercambioSinFechaDestinoEs400() throws Exception {
        mvc.perform(get("/api/v1/servicios/{s}/guardias/{g}/receptores-candidatos", servicio, guardiaAna)
                        .param("tipo", "CAMBIO").session(sesion))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listadoDeCambiosDelRango() throws Exception {
        long guardiaBruno = datos.guardia(bruno, DIA_10.plusDays(5), TipoGuardia.LOCALIZADA, false, servicio);
        registrar("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + guardiaAna + ",\"cedenteId\":" + ana
                + ",\"receptorId\":" + bruno + "}").andExpect(status().isCreated());
        long otraDeAna = datos.guardia(ana, DIA_10.plusDays(3), TipoGuardia.REFUERZO, false, servicio);
        registrar("{\"tipo\":\"CAMBIO\",\"guardiaOrigenId\":" + otraDeAna + ",\"cedenteId\":" + ana
                + ",\"receptorId\":" + bruno + ",\"guardiaDestinoId\":" + guardiaBruno + "}")
                .andExpect(status().isCreated());
        // Un cambio que solo conoce el legacy (sin canal APP) no se lista.
        jdbc.update("INSERT INTO guardias_cambios (TipoCambio, FechaIniCambio, FechaFinCambio, IdMedicoSolicitante,"
                + " IdMedicoDestino, Estado, FechaCreacion, IdServicio) VALUES ('CESION', '2030-02-20', '2030-02-20',"
                + " ?, ?, 'APROBADA', '2030-01-10', ?)", ana, bruno, servicio);

        mvc.perform(get("/api/v1/servicios/{s}/cambios", servicio)
                        .param("desde", "2030-02-01").param("hasta", "2030-02-28").session(sesion))
                .andExpect(status().isOk())
                .andExpect(cumple())
                .andExpect(jsonPath("$.length()").value(2))
                // Más reciente primero.
                .andExpect(jsonPath("$[0].tipo").value("CAMBIO"))
                .andExpect(jsonPath("$[0].fechaDestino").value("2030-02-15"))
                .andExpect(jsonPath("$[1].tipo").value("CESION"))
                .andExpect(jsonPath("$[1].fechaDestino").doesNotExist())
                .andExpect(jsonPath("$[1].guardiaDestino").doesNotExist());

        mvc.perform(get("/api/v1/servicios/{s}/cambios", servicio)
                        .param("desde", "2030-03-01").param("hasta", "2030-03-31").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listadoConRangoInvertidoEs400() throws Exception {
        mvc.perform(get("/api/v1/servicios/{s}/cambios", servicio)
                        .param("desde", "2030-02-28").param("hasta", "2030-02-01").session(sesion))
                // El contrato no declara 400 en esta operación (pendiente de decidir si se añade).
                .andExpect(status().isBadRequest());
    }

    @Test
    void listadoDeOtroServicioEs403() throws Exception {
        long otro = datos.servicio("Neumología");
        mvc.perform(get("/api/v1/servicios/{s}/cambios", otro)
                        .param("desde", "2030-02-01").param("hasta", "2030-02-28").session(sesion))
                .andExpect(status().isForbidden())
                .andExpect(cumple());
    }

    @Test
    void receptoresCandidatosDeGuardiaInexistenteEs404() throws Exception {
        mvc.perform(get("/api/v1/servicios/{s}/guardias/{g}/receptores-candidatos", servicio, 999999)
                        .param("tipo", "CESION").session(sesion))
                .andExpect(status().isNotFound())
                .andExpect(cumple());
    }
}
