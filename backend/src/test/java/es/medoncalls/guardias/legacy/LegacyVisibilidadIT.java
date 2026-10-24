package es.medoncalls.guardias.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;

/** Tras una cesión, el legacy la ve con su propia SQL (FR-026, SC-008; T034). */
class LegacyVisibilidadIT extends IntegracionBase {

    private static final LocalDate DIA_10 = LocalDate.of(2030, 2, 10);

    @Test
    void elCalendarioYElComputoDelLegacyVenLaCesion() throws Exception {
        long servicio = datos.servicio("Cardiología");
        long admin = datos.adjunto("Admin");
        long ana = datos.adjunto("Ana");
        long bruno = datos.adjunto("Bruno");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, ana);
        datos.miembro(servicio, bruno);
        long guardia = datos.guardia(ana, DIA_10, TipoGuardia.PRESENCIA, true, servicio);

        mvc.perform(post("/api/v1/servicios/{id}/cambios", servicio).with(csrfSpa()).session(login(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + guardia + ",\"cedenteId\":" + ana
                                + ",\"receptorId\":" + bruno + "}"))
                .andExpect(status().isCreated());

        NamedParameterJdbcTemplate legacy = new NamedParameterJdbcTemplate(jdbc);
        List<Map<String, Object>> dia = legacy.queryForList(LegacySql.CALENDARIO,
                Map.of("servicioId", servicio, "fecha", DIA_10.toString()));
        assertThat(dia).hasSize(1);
        assertThat(dia.get(0)).containsEntry("IdMedico", (int) bruno).containsEntry("Tipo", "presencia")
                .containsEntry("Festivo", 1);

        // Con FechaFinCambio = FechaIniCambio (R10), el cómputo del legacy encuentra la guardia cedida.
        List<Map<String, Object>> hechos = legacy.queryForList(LegacySql.TOTAL_CAMBIOS_HECHOS, Map.of(
                "idMedicoSolicitante", ana, "fechaIniCambio", "2030-02-01", "fechaFinCambio", "2030-02-28",
                "tipoCambio", "CESION", "estado", "APROBADA"));
        assertThat(hechos).hasSize(1);
        assertThat(hechos.get(0)).containsEntry("TipoCambio", "presencia").containsEntry("UsuarioAprobacion", 1);
        assertThat(((Number) hechos.get(0).get("IdCambio")).intValue()).isEqualTo(1);
    }
}
