package es.medoncalls.guardias.legacy;

import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mock.web.MockHttpSession;

import es.medoncalls.guardias.support.IntegracionBase;

/**
 * Lo que escribe la aplicación nueva en `vacaciones_medicos` lo lee el legacy con su propia SQL: la
 * ficha del médico, el listado, el reparto y `NoTieneVacaciones`, que usan la generación y el
 * relleno de la agenda (002 FR-016, SC-006; quickstart 12).
 */
class LegacyVacacionesIT extends IntegracionBase {

    private static final LocalDate AGO_03 = LocalDate.of(2030, 8, 3);
    private static final LocalDate AGO_04 = LocalDate.of(2030, 8, 4);
    private static final LocalDate SEP_01 = LocalDate.of(2030, 9, 1);
    private static final LocalDate PREVIO = LocalDate.of(2030, 7, 1);

    private long servicio;
    private long bruno;
    private MockHttpSession sesion;

    @BeforeEach
    void datosDelServicio() throws Exception {
        servicio = datos.servicio("Cardiología");
        long admin = datos.adjunto("Ana");
        bruno = datos.adjunto("Bruno");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, bruno);
        sesion = login(admin);
    }

    private NamedParameterJdbcTemplate legacy() {
        return new NamedParameterJdbcTemplate(jdbc);
    }

    private void modificar(String cuerpo) throws Exception {
        mvc.perform(patch("/api/v1/servicios/{s}/medicos/{m}/vacaciones", servicio, bruno).with(csrfSpa())
                        .session(sesion).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk());
    }

    /** `ProcesarMedicos.NoTieneVacaciones`: true si la consulta del día no devuelve filas. */
    private boolean noTieneVacaciones(LocalDate dia) {
        return legacy().queryForList(LegacySql.VACACIONES_DIA, Map.of("idMedico", bruno, "dia", dia.toString()))
                .isEmpty();
    }

    private List<String> diasDelRango(String desde, String hasta) {
        return legacy().queryForList(LegacySql.VACACIONES_RANGO,
                Map.of("idMedico", bruno, "desde", desde, "hasta", hasta)).stream()
                .map(fila -> (String) fila.get("FDay")).sorted().toList();
    }

    @Test
    void elLegacyVeLosDiasAnadidosPorLaApi() throws Exception {
        datos.vacaciones(bruno, PREVIO);

        modificar("{\"anadir\":[\"2030-08-03\",\"2030-08-04\",\"2030-09-01\"]}");

        assertThat(noTieneVacaciones(AGO_03)).isFalse();
        assertThat(noTieneVacaciones(AGO_04)).isFalse();
        assertThat(noTieneVacaciones(SEP_01)).isFalse();
        assertThat(noTieneVacaciones(LocalDate.of(2030, 8, 5))).isTrue();

        assertThat(diasDelRango("2030-07-01", "2030-12-31"))
                .containsExactly("2030-07-01", "2030-08-03", "2030-08-04", "2030-09-01");

        List<Integer> meses = legacy().queryForList(LegacySql.MESES_RANGO,
                        Map.of("idMedico", bruno, "desde", "2030-08-01", "hasta", "2030-12-31")).stream()
                .map(fila -> ((Number) fila.get("fday")).intValue()).sorted().toList();
        assertThat(meses).containsExactly(8, 9);

        Number recuento = (Number) legacy().queryForMap(LegacySql.RECUENTO_RANGO,
                Map.of("idMedico", bruno, "desde", "2030-08-01", "hasta", "2030-08-31")).get("idmedico");
        assertThat(recuento.intValue()).isEqualTo(2);
    }

    @Test
    void elLegacyDejaDeVerLosDiasQuitadosPorLaApiYSigueViendoLosDemas() throws Exception {
        datos.vacaciones(bruno, AGO_03);
        datos.vacaciones(bruno, AGO_04);
        datos.vacaciones(bruno, SEP_01);

        modificar("{\"quitar\":[\"2030-08-04\",\"2030-09-01\"]}");

        assertThat(noTieneVacaciones(AGO_03)).isFalse();
        assertThat(noTieneVacaciones(AGO_04)).isTrue();
        assertThat(noTieneVacaciones(SEP_01)).isTrue();
        assertThat(diasDelRango("2030-07-01", "2030-12-31")).containsExactly("2030-08-03");
    }
}
