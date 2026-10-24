package es.medoncalls.guardias.integration;

import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;

/** Historia 2, escenarios 1–5, más intercambio en el mismo día y concurrencia con una cesión (T045). */
class RegistrarIntercambioIT extends IntegracionBase {

    private static final LocalDate DIA_10 = LocalDate.of(2030, 2, 10);
    private static final LocalDate DIA_15 = LocalDate.of(2030, 2, 15);

    private long servicio;
    private long ana;
    private long bruno;
    private long localizadaAna10;
    private long presenciaBruno15;
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
        localizadaAna10 = datos.guardia(ana, DIA_10, TipoGuardia.LOCALIZADA, false, servicio);
        presenciaBruno15 = datos.guardia(bruno, DIA_15, TipoGuardia.PRESENCIA, true, servicio);
        sesion = login(admin);
    }

    private MockHttpServletRequestBuilder peticion(long origen, long cedente, long destino, long receptor) {
        return post("/api/v1/servicios/{id}/cambios", servicio).with(csrfSpa()).session(sesion)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"CAMBIO\",\"guardiaOrigenId\":" + origen + ",\"cedenteId\":" + cedente
                        + ",\"receptorId\":" + receptor + ",\"guardiaDestinoId\":" + destino + "}");
    }

    private ResultActions intercambiar(long origen, long cedente, long destino, long receptor) throws Exception {
        return mvc.perform(peticion(origen, cedente, destino, receptor));
    }

    private void assertSinCambios() {
        assertThat(datos.medicoDeGuardia(localizadaAna10)).isEqualTo(ana);
        assertThat(datos.medicoDeGuardia(presenciaBruno15)).isEqualTo(bruno);
        assertThat(datos.contar("guardias_cambios")).isZero();
    }

    @Test
    void escenario1_intercambioValido_tipoYFestivoSeQuedanConElDia() throws Exception {
        intercambiar(localizadaAna10, ana, presenciaBruno15, bruno)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CAMBIO"))
                .andExpect(jsonPath("$.fechaOrigen").value("2030-02-10"))
                .andExpect(jsonPath("$.fechaDestino").value("2030-02-15"))
                .andExpect(jsonPath("$.guardiaOrigen.tipo").value("LOCALIZADA"))
                .andExpect(jsonPath("$.guardiaOrigen.festivo").value(false))
                .andExpect(jsonPath("$.guardiaDestino.tipo").value("PRESENCIA"))
                .andExpect(jsonPath("$.guardiaDestino.festivo").value(true));

        Map<String, Object> dia10 = jdbc.queryForMap("SELECT * FROM guardias_medicos WHERE Id = ?", localizadaAna10);
        assertThat(dia10).containsEntry("IdMedico", (int) bruno).containsEntry("Tipo", "localizada")
                .containsEntry("Festivo", 0).containsEntry("FGuardia", "2030-02-10");
        Map<String, Object> dia15 = jdbc.queryForMap("SELECT * FROM guardias_medicos WHERE Id = ?", presenciaBruno15);
        assertThat(dia15).containsEntry("IdMedico", (int) ana).containsEntry("Tipo", "presencia")
                .containsEntry("Festivo", 1).containsEntry("FGuardia", "2030-02-15");

        assertThat(jdbc.queryForMap("SELECT * FROM guardias_cambios"))
                .containsEntry("TipoCambio", "CAMBIO")
                .containsEntry("FechaIniCambio", "2030-02-10")
                .containsEntry("FechaFinCambio", "2030-02-15")
                .containsEntry("IdMedicoSolicitante", (int) ana)
                .containsEntry("IdMedicoDestino", (int) bruno)
                .containsEntry("TipoGuardiaOrigen", "localizada")
                .containsEntry("FestivoOrigen", 0)
                .containsEntry("TipoGuardiaDestino", "presencia")
                .containsEntry("FestivoDestino", 1);
    }

    @Test
    void escenario2_elReceptorNoTieneLaGuardiaDestino() throws Exception {
        long carla = datos.adjunto("Carla");
        datos.miembro(servicio, carla);
        long deCarla = datos.guardia(carla, DIA_15.plusDays(1), TipoGuardia.LOCALIZADA, false, servicio);
        intercambiar(localizadaAna10, ana, deCarla, bruno)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-006"))
                .andExpect(jsonPath("$.infracciones[0].medicoId").value(bruno));
        assertSinCambios();
    }

    @Test
    void escenario3_laFechaDestinoEsPasada() throws Exception {
        long pasada = datos.guardia(bruno, HOY.minusDays(2), TipoGuardia.LOCALIZADA, false, servicio);
        intercambiar(localizadaAna10, ana, pasada, bruno)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-009"));
        assertThat(datos.medicoDeGuardia(localizadaAna10)).isEqualTo(ana);
    }

    @Test
    void escenario4_presenciasConsecutivasDeQuienRecibeLaPresencia() throws Exception {
        datos.guardia(ana, DIA_15.minusDays(1), TipoGuardia.PRESENCIA, false, servicio);
        intercambiar(localizadaAna10, ana, presenciaBruno15, bruno)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-013"))
                .andExpect(jsonPath("$.infracciones[0].medicoId").value(ana))
                .andExpect(jsonPath("$.infracciones[0].fecha").value("2030-02-14"));
        assertSinCambios();
    }

    @Test
    void escenario5_elDiaAdyacenteQueEsLaOtraFechaDelCambioNoCuenta() throws Exception {
        long presenciaAna20 = datos.guardia(ana, DIA_10.plusDays(10), TipoGuardia.PRESENCIA, false, servicio);
        long presenciaBruno21 = datos.guardia(bruno, DIA_10.plusDays(11), TipoGuardia.PRESENCIA, false, servicio);
        intercambiar(presenciaAna20, ana, presenciaBruno21, bruno).andExpect(status().isCreated());
        assertThat(datos.medicoDeGuardia(presenciaAna20)).isEqualTo(bruno);
        assertThat(datos.medicoDeGuardia(presenciaBruno21)).isEqualTo(ana);
    }

    @Test
    void intercambioEnElMismoDia_noChocaConElIndiceUnico() throws Exception {
        long presenciaBruno10 = datos.guardia(bruno, DIA_10, TipoGuardia.PRESENCIA, false, servicio);
        intercambiar(localizadaAna10, ana, presenciaBruno10, bruno).andExpect(status().isCreated());
        assertThat(datos.medicoDeGuardia(localizadaAna10)).isEqualTo(bruno);
        assertThat(datos.medicoDeGuardia(presenciaBruno10)).isEqualTo(ana);
    }

    @Test
    void guardiaDestinoDeOtroServicioEs404() throws Exception {
        long otroServicio = datos.servicio("Neumología");
        long ajena = datos.guardia(bruno, DIA_15.plusDays(3), TipoGuardia.PRESENCIA, false, otroServicio);
        intercambiar(localizadaAna10, ana, ajena, bruno).andExpect(status().isNotFound());
        assertSinCambios();
    }

    @Test
    void intercambioYCesionSimultaneosDeLaMismaGuardia_soloUnoSeAplica() throws Exception {
        long carla = datos.adjunto("Carla");
        datos.miembro(servicio, carla);
        MockHttpServletRequestBuilder cesion = post("/api/v1/servicios/{id}/cambios", servicio).with(csrfSpa())
                .session(sesion).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + localizadaAna10 + ",\"cedenteId\":" + ana
                        + ",\"receptorId\":" + carla + "}");
        MockHttpServletRequestBuilder intercambio = peticion(localizadaAna10, ana, presenciaBruno15, bruno);

        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Future<Integer>> resultados = new ArrayList<>();
            for (MockHttpServletRequestBuilder p : List.of(cesion, intercambio)) {
                Callable<Integer> tarea = () -> {
                    salida.await();
                    return mvc.perform(p).andReturn().getResponse().getStatus();
                };
                resultados.add(hilos.submit(tarea));
            }
            salida.countDown();
            List<Integer> estados = new ArrayList<>();
            for (Future<Integer> r : resultados) {
                estados.add(r.get());
            }
            assertThat(estados).containsExactlyInAnyOrder(201, 422);
        } finally {
            hilos.shutdownNow();
        }
        assertThat(datos.contar("guardias_cambios")).isEqualTo(1);
    }
}
