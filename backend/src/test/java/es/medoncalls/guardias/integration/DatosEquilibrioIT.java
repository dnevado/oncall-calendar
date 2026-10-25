package es.medoncalls.guardias.integration;

import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;

/** Historia 4: cada cambio guarda tipo y festivo de las guardias tal como eran antes (FR-003, FR-023; T056). */
class DatosEquilibrioIT extends IntegracionBase {

    private static final LocalDate DIA_10 = LocalDate.of(2030, 2, 10);
    private static final LocalDate DIA_20 = LocalDate.of(2030, 2, 20);

    private long servicio;
    private long ana;
    private long bruno;
    private MockHttpSession sesion;

    @BeforeEach
    void plantilla() throws Exception {
        servicio = datos.servicio("Cardiología");
        long admin = datos.adjunto("Admin");
        ana = datos.adjunto("Ana");
        bruno = datos.adjunto("Bruno");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, ana);
        datos.miembro(servicio, bruno);
        sesion = login(admin);
    }

    private org.springframework.test.web.servlet.ResultActions registrar(String cuerpo) throws Exception {
        return mvc.perform(post("/api/v1/servicios/{id}/cambios", servicio).with(csrfSpa()).session(sesion)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private Map<String, Object> cambioGuardado() {
        return jdbc.queryForMap("SELECT * FROM guardias_cambios");
    }

    @Test
    void escenarios1y2_laCesionGuardaTipoYFestivoDeLaGuardiaCedida_noLosDeOtrasDelReceptor() throws Exception {
        long presenciaFestiva = datos.guardia(ana, DIA_10, TipoGuardia.PRESENCIA, true, servicio);
        datos.guardia(bruno, DIA_20, TipoGuardia.LOCALIZADA, false, servicio);

        registrar("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + presenciaFestiva + ",\"cedenteId\":" + ana
                + ",\"receptorId\":" + bruno + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guardiaOrigen.tipo").value("PRESENCIA"))
                .andExpect(jsonPath("$.guardiaOrigen.festivo").value(true))
                .andExpect(jsonPath("$.guardiaDestino").doesNotExist());

        assertThat(cambioGuardado())
                .containsEntry("TipoCambio", "CESION")
                .containsEntry("IdMedicoSolicitante", (int) ana)
                .containsEntry("IdMedicoDestino", (int) bruno)
                .containsEntry("TipoGuardiaOrigen", "presencia")
                .containsEntry("FestivoOrigen", 1)
                .containsEntry("TipoGuardiaDestino", null)
                .containsEntry("FestivoDestino", null);
    }

    @Test
    void cesionDeUnaLocalizadaNoFestiva() throws Exception {
        long localizada = datos.guardia(ana, DIA_10, TipoGuardia.LOCALIZADA, false, servicio);
        datos.guardia(bruno, DIA_20, TipoGuardia.PRESENCIA, true, servicio);

        registrar("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + localizada + ",\"cedenteId\":" + ana
                + ",\"receptorId\":" + bruno + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guardiaOrigen.tipo").value("LOCALIZADA"))
                .andExpect(jsonPath("$.guardiaOrigen.festivo").value(false));

        assertThat(cambioGuardado()).containsEntry("TipoGuardiaOrigen", "localizada").containsEntry("FestivoOrigen", 0);
    }

    @Test
    void elIntercambioGuardaLasDosGuardias() throws Exception {
        long refuerzoAna = datos.guardia(ana, DIA_10, TipoGuardia.REFUERZO, false, servicio);
        long presenciaBruno = datos.guardia(bruno, DIA_20, TipoGuardia.PRESENCIA, true, servicio);

        registrar("{\"tipo\":\"CAMBIO\",\"guardiaOrigenId\":" + refuerzoAna + ",\"cedenteId\":" + ana
                + ",\"receptorId\":" + bruno + ",\"guardiaDestinoId\":" + presenciaBruno + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guardiaOrigen.tipo").value("REFUERZO"))
                .andExpect(jsonPath("$.guardiaOrigen.festivo").value(false))
                .andExpect(jsonPath("$.guardiaDestino.tipo").value("PRESENCIA"))
                .andExpect(jsonPath("$.guardiaDestino.festivo").value(true));

        assertThat(cambioGuardado())
                .containsEntry("TipoGuardiaOrigen", "refuerzo")
                .containsEntry("FestivoOrigen", 0)
                .containsEntry("TipoGuardiaDestino", "presencia")
                .containsEntry("FestivoDestino", 1);
    }

    @Test
    void laCesionDeUnResidenteGuardaElTipoResidente() throws Exception {
        long rita = datos.residente("Rita", "R2");
        long raul = datos.residente("Raúl", "R4");
        datos.miembro(servicio, rita);
        datos.miembro(servicio, raul);
        long deRita = datos.guardia(rita, DIA_10, TipoGuardia.RESIDENTE, false, servicio);
        // El ajuste R3–R5 cambia otra guardia del día; lo guardado es la cedida, antes del cambio.
        datos.guardia(ana, DIA_10, TipoGuardia.REFUERZO, false, servicio);

        registrar("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + deRita + ",\"cedenteId\":" + rita
                + ",\"receptorId\":" + raul + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guardiaOrigen.tipo").value("RESIDENTE"));

        assertThat(cambioGuardado()).containsEntry("TipoGuardiaOrigen", "").containsEntry("FestivoOrigen", 0);
    }
}
