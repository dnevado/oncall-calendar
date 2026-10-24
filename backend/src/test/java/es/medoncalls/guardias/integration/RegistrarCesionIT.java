package es.medoncalls.guardias.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;

/** Historia 1, escenarios 1–8, más atomicidad, concurrencia, idempotencia y permisos (T032). */
class RegistrarCesionIT extends IntegracionBase {

    private static final LocalDate DIA_10 = LocalDate.of(2030, 2, 10);

    private long servicio;
    private long admin;
    private long ana;
    private long bruno;
    private long guardiaAna;
    private MockHttpSession sesion;

    @BeforeEach
    void calendario() throws Exception {
        servicio = datos.servicio("Cardiología");
        admin = datos.adjunto("Admin");
        ana = datos.adjunto("Ana");
        bruno = datos.adjunto("Bruno");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, ana);
        datos.miembro(servicio, bruno);
        guardiaAna = datos.guardia(ana, DIA_10, TipoGuardia.PRESENCIA, true, servicio);
        sesion = login(admin);
    }

    @AfterEach
    void quitarFallo() {
        jdbc.execute("DROP TRIGGER IF EXISTS prueba_falla_alta_cambio");
    }

    private static String cuerpo(long guardiaId, long cedenteId, long receptorId) {
        return "{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + guardiaId + ",\"cedenteId\":" + cedenteId
                + ",\"receptorId\":" + receptorId + "}";
    }

    private MockHttpServletRequestBuilder peticion(long guardiaId, long cedenteId, long receptorId) {
        return post("/api/v1/servicios/{id}/cambios", servicio).with(csrfSpa()).session(sesion)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo(guardiaId, cedenteId, receptorId));
    }

    private ResultActions ceder(long guardiaId, long cedenteId, long receptorId) throws Exception {
        return mvc.perform(peticion(guardiaId, cedenteId, receptorId));
    }

    private void assertSinCambios() {
        assertThat(datos.medicoDeGuardia(guardiaAna)).isEqualTo(ana);
        assertThat(datos.contar("guardias_cambios")).isZero();
    }

    @Test
    void escenario1_cesionValidaPasaLaGuardiaConElMismoTipoYFestivo() throws Exception {
        ceder(guardiaAna, ana, bruno)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CESION"))
                .andExpect(jsonPath("$.fechaOrigen").value("2030-02-10"))
                .andExpect(jsonPath("$.fechaDestino").doesNotExist())
                .andExpect(jsonPath("$.cedente.id").value(ana))
                .andExpect(jsonPath("$.receptor.id").value(bruno))
                .andExpect(jsonPath("$.guardiaOrigen.tipo").value("PRESENCIA"))
                .andExpect(jsonPath("$.guardiaOrigen.festivo").value(true))
                .andExpect(jsonPath("$.registradoPor.id").value(admin));

        Map<String, Object> guardia = jdbc.queryForMap("SELECT * FROM guardias_medicos WHERE Id = ?", guardiaAna);
        assertThat(guardia).containsEntry("IdMedico", (int) bruno).containsEntry("Tipo", "presencia")
                .containsEntry("Festivo", 1).containsEntry("FGuardia", "2030-02-10");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM guardias_medicos WHERE IdMedico = ? AND FGuardia = ?",
                Integer.class, ana, "2030-02-10")).isZero();

        Map<String, Object> cambio = jdbc.queryForMap("SELECT * FROM guardias_cambios");
        assertThat(cambio)
                .containsEntry("TipoCambio", "CESION")
                .containsEntry("FechaIniCambio", "2030-02-10")
                .containsEntry("FechaFinCambio", "2030-02-10")
                .containsEntry("IdMedicoSolicitante", (int) ana)
                .containsEntry("IdMedicoDestino", (int) bruno)
                .containsEntry("Estado", "APROBADA")
                .containsEntry("UsuarioAprobacion", (int) admin)
                .containsEntry("FechaCreacion", HOY.toString())
                .containsEntry("FechaAprobacion", HOY.toString())
                .containsEntry("IdServicio", (int) servicio)
                .containsEntry("TipoGuardiaOrigen", "presencia")
                .containsEntry("FestivoOrigen", 1)
                .containsEntry("Canal", "APP");
        assertThat(cambio.get("TipoGuardiaDestino")).isNull();
        assertThat(cambio.get("FechaHoraRegistro")).isNotNull();
    }

    @Test
    void escenario2_receptorConGuardiaEseDia() throws Exception {
        datos.guardia(bruno, DIA_10, TipoGuardia.LOCALIZADA, false, servicio);
        ceder(guardiaAna, ana, bruno)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-011"))
                .andExpect(jsonPath("$.infracciones[0].medicoId").value(bruno))
                .andExpect(jsonPath("$.infracciones[0].fecha").value("2030-02-10"));
        assertSinCambios();
    }

    @Test
    void escenario3_receptorDeVacacionesEnCualquierServicio() throws Exception {
        datos.vacaciones(bruno, DIA_10);
        ceder(guardiaAna, ana, bruno)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-012"));
        assertSinCambios();
    }

    @Test
    void escenario4_presenciaConsecutivaTambienParaResidenteSimulado() throws Exception {
        long rita = datos.residente("Rita", "SIMULADO");
        long sara = datos.residente("Sara", "SIMULADO");
        datos.miembro(servicio, rita);
        datos.miembro(servicio, sara);
        long guardiaRita = datos.guardia(rita, DIA_10, TipoGuardia.PRESENCIA, false, servicio);
        datos.guardia(sara, DIA_10.plusDays(1), TipoGuardia.PRESENCIA, false, servicio);

        ceder(guardiaRita, rita, sara)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-013"))
                .andExpect(jsonPath("$.infracciones[0].fecha").value("2030-02-11"));
        assertThat(datos.medicoDeGuardia(guardiaRita)).isEqualTo(rita);
    }

    @Test
    void escenario5_tipoDeMedicoDistintoEnLaFecha() throws Exception {
        long rita = datos.residente("Rita", "R2");
        datos.miembro(servicio, rita);
        ceder(guardiaAna, ana, rita)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-014"));

        // Con el histórico, Rita ya es adjunta en la fecha de la guardia: se acepta.
        datos.cambiarTipoDesde(rita, "ADJUNTO", null, DIA_10.minusDays(5));
        ceder(guardiaAna, ana, rita).andExpect(status().isCreated());
    }

    @Test
    void escenario6_receptorInactivoNoEsCandidatoYSeRechaza() throws Exception {
        jdbc.update("UPDATE medicos SET Activo = 0 WHERE ID = ?", bruno);
        mvc.perform(get("/api/v1/servicios/{s}/guardias/{g}/receptores-candidatos", servicio, guardiaAna)
                        .param("tipo", "CESION").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.medico.id == " + bruno + ")]").doesNotExist());

        ceder(guardiaAna, ana, bruno)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-015"));
        assertSinCambios();
    }

    @Test
    void candidatosConVariosMedicos_cadaUnoSeEvaluaConSusPropiosDatos() throws Exception {
        long carla = datos.adjunto("Carla");
        long david = datos.adjunto("David");
        long elena = datos.adjunto("Elena");
        long rita = datos.residente("Rita", "R2");
        for (long medico : new long[] {carla, david, elena, rita}) {
            datos.miembro(servicio, medico);
        }
        datos.vacaciones(carla, DIA_10);
        datos.guardia(david, DIA_10.plusDays(1), TipoGuardia.PRESENCIA, false, servicio);
        datos.guardia(elena, DIA_10.plusDays(1), TipoGuardia.LOCALIZADA, false, servicio);

        mvc.perform(get("/api/v1/servicios/{s}/guardias/{g}/receptores-candidatos", servicio, guardiaAna)
                        .param("tipo", "CESION").session(sesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].medico.id").value(
                        org.hamcrest.Matchers.containsInAnyOrder((int) admin, (int) bruno, (int) elena)));
    }

    @Test
    void escenario7_guardiaDeHoyOPasadaSeRechaza() throws Exception {
        long deHoy = datos.guardia(ana, HOY, TipoGuardia.LOCALIZADA, false, servicio);
        long pasada = datos.guardia(ana, HOY.minusDays(3), TipoGuardia.LOCALIZADA, false, servicio);
        ceder(deHoy, ana, bruno).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-008"));
        ceder(pasada, ana, bruno).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-008"));
        assertThat(datos.contar("guardias_cambios")).isZero();
    }

    @Test
    void escenario8_receptorSinNingunaGuardiaSeAcepta() throws Exception {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM guardias_medicos WHERE IdMedico = ?", Integer.class, bruno))
                .isZero();
        ceder(guardiaAna, ana, bruno).andExpect(status().isCreated());
    }

    @Test
    void variasInfraccionesSeDevuelvenJuntas() throws Exception {
        long rita = datos.residente("Rita", "R2");
        datos.miembro(servicio, rita);
        datos.vacaciones(rita, DIA_10);
        datos.guardia(rita, DIA_10.minusDays(1), TipoGuardia.PRESENCIA, false, servicio);
        ceder(guardiaAna, ana, rita)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones.length()").value(3))
                .andExpect(jsonPath("$.infracciones[*].regla").value(
                        org.hamcrest.Matchers.containsInAnyOrder("CG-012", "CG-013", "CG-014")));
    }

    @Test
    void atomicidad_unFalloAlRegistrarDejaElCalendarioSinCambios() throws Exception {
        // Fallo real después de actualizar guardias_medicos: el alta en guardias_cambios lo provoca.
        jdbc.execute("CREATE TRIGGER prueba_falla_alta_cambio BEFORE INSERT ON guardias_cambios FOR EACH ROW"
                + " SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'fallo inyectado'");
        ceder(guardiaAna, ana, bruno).andExpect(status().is5xxServerError());
        assertSinCambios();
    }

    @Test
    void dosCesionesSimultaneasDeLaMismaGuardia_soloUnaSeAplica() throws Exception {
        long carla = datos.adjunto("Carla");
        datos.miembro(servicio, carla);
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Future<Integer>> resultados = new ArrayList<>();
            for (long receptor : new long[] {bruno, carla}) {
                Callable<Integer> tarea = () -> {
                    salida.await();
                    return mvc.perform(peticion(guardiaAna, ana, receptor)).andReturn().getResponse().getStatus();
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

    @Test
    void cesionSimultaneaDelDiaSiguienteAlMismoReceptor_seVeAntesDeEvaluarCg013() throws Exception {
        long carla = datos.adjunto("Carla");
        datos.miembro(servicio, carla);
        long guardiaCarla = datos.guardia(carla, DIA_10.plusDays(1), TipoGuardia.PRESENCIA, false, servicio);
        CountDownLatch bloqueado = new CountDownLatch(1);
        ExecutorService hilo = Executors.newSingleThreadExecutor();
        try {
            // Otra transacción en curso hace lo mismo que el registro: bloquea el servicio y da el día 11 al receptor.
            Future<?> otra = hilo.submit(() -> {
                try (Connection conexion = jdbc.getDataSource().getConnection()) {
                    conexion.setAutoCommit(false);
                    try (Statement sql = conexion.createStatement()) {
                        sql.executeQuery("SELECT IdServicio FROM guardias_servicios WHERE IdServicio = " + servicio
                                + " FOR UPDATE").close();
                        sql.executeUpdate("UPDATE guardias_medicos SET IdMedico = " + bruno + " WHERE Id = " + guardiaCarla);
                    }
                    bloqueado.countDown();
                    Thread.sleep(500);
                    conexion.commit();
                }
                return null;
            });
            bloqueado.await();
            ceder(guardiaAna, ana, bruno)
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.infracciones[0].regla").value("CG-013"))
                    .andExpect(jsonPath("$.infracciones[0].fecha").value("2030-02-11"));
            otra.get();
        } finally {
            hilo.shutdownNow();
        }
        assertSinCambios();
    }

    @Test
    void idempotencyKeyDeMasDe64CaracteresEs400() throws Exception {
        mvc.perform(peticion(guardiaAna, ana, bruno).header("Idempotency-Key", "k".repeat(65)))
                .andExpect(status().isBadRequest());
        assertSinCambios();
    }

    @Test
    void idempotencyKeyRepetidaSeAplicaUnaVezYDevuelveElMismoCambio() throws Exception {
        String primera = mvc.perform(peticion(guardiaAna, ana, bruno).header("Idempotency-Key", "clave-1"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String segunda = mvc.perform(peticion(guardiaAna, ana, bruno).header("Idempotency-Key", "clave-1"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        assertThat(segunda).isEqualTo(primera);
        assertThat(datos.contar("guardias_cambios")).isEqualTo(1);
    }

    @Test
    void idempotencyKeyConOtraPeticionEs409() throws Exception {
        long carla = datos.adjunto("Carla");
        datos.miembro(servicio, carla);
        mvc.perform(peticion(guardiaAna, ana, bruno).header("Idempotency-Key", "clave-2"))
                .andExpect(status().isCreated());
        mvc.perform(peticion(guardiaAna, bruno, carla).header("Idempotency-Key", "clave-2"))
                .andExpect(status().isConflict());
        assertThat(datos.medicoDeGuardia(guardiaAna)).isEqualTo(bruno);
    }

    @Test
    void repetirSinClaveEsRechazadoPorCg005() throws Exception {
        ceder(guardiaAna, ana, bruno).andExpect(status().isCreated());
        ceder(guardiaAna, ana, bruno)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-005"));
        assertThat(datos.contar("guardias_cambios")).isEqualTo(1);
    }

    @Test
    void unMedicoQueNoAdministraElServicioRecibe403() throws Exception {
        sesion = login(ana);
        ceder(guardiaAna, ana, bruno).andExpect(status().isForbidden());
        assertSinCambios();
    }

    @Test
    void receptorDeOtroServicioEsCg015() throws Exception {
        long otroServicio = datos.servicio("Neumología");
        long david = datos.adjunto("David");
        datos.miembro(otroServicio, david);
        ceder(guardiaAna, ana, david)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-015"));
        assertSinCambios();
    }

    @Test
    void receptorConActivoServicioCeroPeroActivoYMiembroSeAcepta() throws Exception {
        jdbc.update("UPDATE guardias_servicios_medicos SET ActivoServicio = 0 WHERE IdMedico = ?", bruno);
        ceder(guardiaAna, ana, bruno).andExpect(status().isCreated());
    }

    @Test
    void elInterruptorDelLegacyNoAfectaALaApi() throws Exception {
        datos.configuracion("ACTIVAR_CAMBIO_GUARDIAS", "N", servicio);
        ceder(guardiaAna, ana, bruno).andExpect(status().isCreated());
    }

    @Test
    void guardiaDeOtroServicioEs404() throws Exception {
        long otroServicio = datos.servicio("Neumología");
        long ajena = datos.guardia(ana, DIA_10.plusDays(2), TipoGuardia.PRESENCIA, false, otroServicio);
        ceder(ajena, ana, bruno).andExpect(status().isNotFound());
    }
}
