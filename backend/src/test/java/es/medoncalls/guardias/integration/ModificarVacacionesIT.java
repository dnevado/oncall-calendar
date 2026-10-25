package es.medoncalls.guardias.integration;

import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;

/**
 * Añadir días de vacaciones (002 US1; quickstart 1 a 4, 8, 10 y 11). Hoy es 2030-01-15
 * ({@link IntegracionBase#HOY}).
 */
class ModificarVacacionesIT extends IntegracionBase {

    private static final LocalDate AGO_03 = LocalDate.of(2030, 8, 3);
    private static final LocalDate AGO_10 = LocalDate.of(2030, 8, 10);

    private long servicio;
    private long otroServicio;
    private long admin;
    private long bruno;
    private long dario;
    private MockHttpSession sesion;

    @BeforeEach
    void datosDelServicio() throws Exception {
        servicio = datos.servicio("Cardiología");
        otroServicio = datos.servicio("Neumología");
        admin = datos.adjunto("Ana");
        bruno = datos.adjunto("Bruno");
        dario = datos.adjunto("Darío");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, bruno);
        datos.miembro(otroServicio, dario);
        sesion = login(admin);
    }

    static String lista(List<LocalDate> fechas) {
        return fechas.stream().map(f -> "\"" + f + "\"").collect(Collectors.joining(",", "[", "]"));
    }

    static String cuerpo(List<LocalDate> anadir, List<LocalDate> quitar) {
        return "{\"anadir\":" + lista(anadir) + ",\"quitar\":" + lista(quitar) + "}";
    }

    private MockHttpServletRequestBuilder peticion(long medicoId, List<LocalDate> anadir, List<LocalDate> quitar) {
        return patch("/api/v1/servicios/{s}/medicos/{m}/vacaciones", servicio, medicoId).with(csrfSpa()).session(sesion)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo(anadir, quitar));
    }

    private ResultActions anadir(long medicoId, LocalDate... dias) throws Exception {
        return mvc.perform(peticion(medicoId, List.of(dias), List.of()));
    }

    private static List<LocalDate> rango(LocalDate desde, LocalDate hasta) {
        return desde.datesUntil(hasta.plusDays(1)).toList();
    }

    @Test
    void escenario1_anadeVariosDiasYNoTocaLosDemas() throws Exception {
        LocalDate previo = LocalDate.of(2030, 3, 1);
        datos.vacaciones(bruno, previo);
        List<LocalDate> dias = new ArrayList<>(rango(AGO_03, LocalDate.of(2030, 8, 14)));
        dias.add(LocalDate.of(2030, 12, 24));

        mvc.perform(peticion(bruno, dias, List.of()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anadidos.length()").value(13))
                .andExpect(jsonPath("$.anadidos[0]").value("2030-08-03"))
                .andExpect(jsonPath("$.anadidos[12]").value("2030-12-24"))
                .andExpect(jsonPath("$.quitados.length()").value(0));

        assertThat(datos.diasVacaciones(bruno)).hasSize(14).contains(previo).containsAll(dias);
    }

    @Test
    void escenario2_unDiaConGuardiaEnElServicioRechazaTodoConLaFechaYLaGuardia() throws Exception {
        datos.guardia(bruno, AGO_10, TipoGuardia.PRESENCIA, false, servicio);

        mvc.perform(peticion(bruno, rango(AGO_03, LocalDate.of(2030, 8, 14)), List.of()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(
                        "No se han guardado las vacaciones porque incumplen las reglas indicadas."))
                .andExpect(jsonPath("$.infracciones.length()").value(1))
                .andExpect(jsonPath("$.infracciones[0].regla").value("VAC-003"))
                .andExpect(jsonPath("$.infracciones[0].fecha").value("2030-08-10"))
                .andExpect(jsonPath("$.infracciones[0].medicoId").value(bruno))
                .andExpect(jsonPath("$.infracciones[0].mensaje").value(
                        "Bruno Prueba tiene guardia de presencia el 10/08/2030 en este servicio:"
                                + " no se puede marcar ese día como vacaciones."));

        assertThat(datos.contarVacaciones(bruno)).isZero();
    }

    @Test
    void escenario2_variosDiasConGuardiaSeInformanTodos() throws Exception {
        datos.guardia(bruno, AGO_03, TipoGuardia.LOCALIZADA, false, servicio);
        datos.guardia(bruno, AGO_10, TipoGuardia.PRESENCIA, true, servicio);

        mvc.perform(peticion(bruno, rango(AGO_03, LocalDate.of(2030, 8, 14)), List.of()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones.length()").value(2))
                .andExpect(jsonPath("$.infracciones[0].fecha").value("2030-08-03"))
                .andExpect(jsonPath("$.infracciones[1].fecha").value("2030-08-10"));
    }

    @Test
    void unaGuardiaEnOtroServicioNoImpideAnadir() throws Exception {
        datos.miembro(otroServicio, bruno);
        datos.guardia(bruno, AGO_10, TipoGuardia.PRESENCIA, false, otroServicio);

        anadir(bruno, AGO_10).andExpect(status().isOk());

        assertThat(datos.diasVacaciones(bruno)).containsExactly(AGO_10);
    }

    @Test
    void escenario3_unDiaPasadoSeRechaza_yHoySeAcepta() throws Exception {
        anadir(bruno, HOY.minusDays(5))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("VAC-008"))
                .andExpect(jsonPath("$.infracciones[0].fecha").value("2030-01-10"));
        assertThat(datos.contarVacaciones(bruno)).isZero();

        anadir(bruno, HOY).andExpect(status().isOk()).andExpect(jsonPath("$.anadidos[0]").value("2030-01-15"));
    }

    @Test
    void escenario4_sinLimiteFuturoEIdempotente() throws Exception {
        LocalDate dentroDeTresAnios = HOY.plusYears(3);

        anadir(bruno, dentroDeTresAnios).andExpect(status().isOk()).andExpect(jsonPath("$.anadidos.length()").value(1));
        anadir(bruno, dentroDeTresAnios).andExpect(status().isOk()).andExpect(jsonPath("$.anadidos.length()").value(0));

        assertThat(datos.diasVacaciones(bruno)).containsExactly(dentroDeTresAnios);
    }

    @Test
    void escenario5US1_volverAMarcarUnDiaExistenteNoLoDuplica() throws Exception {
        LocalDate ago05 = LocalDate.of(2030, 8, 5);
        datos.vacaciones(bruno, ago05);

        mvc.perform(peticion(bruno, rango(AGO_03, LocalDate.of(2030, 8, 6)), List.of()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anadidos.length()").value(3));

        assertThat(datos.diasVacaciones(bruno)).containsExactly(AGO_03, LocalDate.of(2030, 8, 4), ago05,
                LocalDate.of(2030, 8, 6));
    }

    @Test
    void escenario8_sinRolEs403_yMedicoDeOtroServicioEs404_sinCambios() throws Exception {
        sesion = login(bruno);
        anadir(bruno, AGO_10).andExpect(status().isForbidden());

        sesion = login(admin);
        anadir(dario, AGO_10).andExpect(status().isNotFound());

        assertThat(datos.contar("vacaciones_medicos")).isZero();
    }

    @Test
    void peticionesMalFormadasSon400() throws Exception {
        mvc.perform(peticion(bruno, List.of(), List.of())).andExpect(status().isBadRequest());
        mvc.perform(peticion(bruno, List.of(AGO_10, AGO_10), List.of())).andExpect(status().isBadRequest());
        mvc.perform(peticion(bruno, List.of(AGO_10), List.of(AGO_10))).andExpect(status().isBadRequest());
        mvc.perform(peticion(bruno, rango(AGO_03, AGO_03.plusDays(366)), List.of())).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/v1/servicios/{s}/medicos/{m}/vacaciones", servicio, bruno).with(csrfSpa())
                        .session(sesion).contentType(MediaType.APPLICATION_JSON).content("{\"anadir\":[\"2030-13-45\"]}"))
                .andExpect(status().isBadRequest());

        assertThat(datos.contar("vacaciones_medicos")).isZero();
    }

    // Quitar días (US2; quickstart 5 a 7).

    private ResultActions quitar(long medicoId, LocalDate... dias) throws Exception {
        return mvc.perform(peticion(medicoId, List.of(), List.of(dias)));
    }

    @Test
    void escenarioUS2_1_quitarDosDiasDeUnPeriodoSoloQuitaEsos() throws Exception {
        rango(AGO_03, LocalDate.of(2030, 8, 14)).forEach(d -> datos.vacaciones(bruno, d));
        LocalDate navidad = LocalDate.of(2030, 12, 24);
        datos.vacaciones(bruno, navidad);

        quitar(bruno, LocalDate.of(2030, 8, 13), LocalDate.of(2030, 8, 14))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anadidos.length()").value(0))
                .andExpect(jsonPath("$.quitados.length()").value(2))
                .andExpect(jsonPath("$.quitados[0]").value("2030-08-13"));

        assertThat(datos.diasVacaciones(bruno)).hasSize(11).contains(AGO_03, LocalDate.of(2030, 8, 12), navidad)
                .doesNotContain(LocalDate.of(2030, 8, 13), LocalDate.of(2030, 8, 14));
    }

    @Test
    void escenarioUS2_2_quitarUnDiaPasadoSeRechazaYElDiaSigue() throws Exception {
        LocalDate pasado = HOY.minusDays(14);
        datos.vacaciones(bruno, pasado);

        quitar(bruno, pasado)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("VAC-008"))
                .andExpect(jsonPath("$.infracciones[0].fecha").value(pasado.toString()));

        assertThat(datos.diasVacaciones(bruno)).containsExactly(pasado);
    }

    @Test
    void escenarioUS2_3_mezclaConUnConflictoNoQuitaNiAnadeNada() throws Exception {
        LocalDate guardado = LocalDate.of(2030, 7, 1);
        datos.vacaciones(bruno, guardado);
        datos.guardia(bruno, AGO_10, TipoGuardia.PRESENCIA, false, servicio);

        mvc.perform(peticion(bruno, List.of(AGO_03, AGO_10), List.of(guardado)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones.length()").value(1))
                .andExpect(jsonPath("$.infracciones[0].regla").value("VAC-003"));

        assertThat(datos.diasVacaciones(bruno)).containsExactly(guardado);
    }

    @Test
    void quitarUnDiaFuturoEnConflictoConUnaGuardiaSePermite() throws Exception {
        datos.vacaciones(bruno, AGO_10);
        datos.guardia(bruno, AGO_10, TipoGuardia.PRESENCIA, false, servicio);

        quitar(bruno, AGO_10).andExpect(status().isOk()).andExpect(jsonPath("$.quitados[0]").value("2030-08-10"));

        assertThat(datos.contarVacaciones(bruno)).isZero();
    }

    @Test
    void quitarUnDiaQueNoEsDeVacacionesNoEsError() throws Exception {
        quitar(bruno, AGO_10).andExpect(status().isOk()).andExpect(jsonPath("$.quitados.length()").value(0));
        quitar(bruno, AGO_10).andExpect(status().isOk());
    }

    @Test
    void anadirYQuitarALaVezSeAplicaEnUnaOperacion() throws Exception {
        LocalDate guardado = LocalDate.of(2030, 7, 1);
        datos.vacaciones(bruno, guardado);

        mvc.perform(peticion(bruno, List.of(AGO_03), List.of(guardado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anadidos[0]").value("2030-08-03"))
                .andExpect(jsonPath("$.quitados[0]").value("2030-07-01"));

        assertThat(datos.diasVacaciones(bruno)).containsExactly(AGO_03);
    }

    @Test
    void dosAdministradoresNoSePisan() throws Exception {
        long otraAdmin = datos.adjunto("Olga");
        datos.miembro(servicio, otraAdmin, true, true);
        LocalDate julio = LocalDate.of(2030, 7, 1);
        datos.vacaciones(bruno, julio);

        anadir(bruno, AGO_03).andExpect(status().isOk());
        sesion = login(otraAdmin);
        quitar(bruno, julio).andExpect(status().isOk());

        assertThat(datos.diasVacaciones(bruno)).containsExactly(AGO_03);
    }

    @Test
    void unFalloAMitadDeOperacionNoGuardaNada() throws Exception {
        LocalDate julio = LocalDate.of(2030, 7, 1);
        datos.vacaciones(bruno, julio);
        // El alta falla después del borrado: la transacción deshace también el borrado (FR-006).
        jdbc.execute("CREATE TRIGGER prueba_falla_alta_vacaciones BEFORE INSERT ON vacaciones_medicos FOR EACH ROW"
                + " SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'fallo de prueba'");
        try {
            mvc.perform(peticion(bruno, List.of(AGO_03), List.of(julio))).andExpect(status().isInternalServerError());
        } finally {
            jdbc.execute("DROP TRIGGER IF EXISTS prueba_falla_alta_vacaciones");
        }

        assertThat(datos.diasVacaciones(bruno)).containsExactly(julio);
    }

    // Concurrencia con el registro de cambios de la 001 (research V3).

    private String cesion(long guardiaId, long cedenteId, long receptorId) {
        return "{\"tipo\":\"CESION\",\"guardiaOrigenId\":" + guardiaId + ",\"cedenteId\":" + cedenteId
                + ",\"receptorId\":" + receptorId + "}";
    }

    private ResultActions ceder(long guardiaId, long cedenteId, long receptorId) throws Exception {
        return mvc.perform(post("/api/v1/servicios/{id}/cambios", servicio).with(csrfSpa()).session(sesion)
                .contentType(MediaType.APPLICATION_JSON).content(cesion(guardiaId, cedenteId, receptorId)));
    }

    @Test
    void cesionConfirmadaMientrasSeEsperaElBloqueo_seVeAntesDeEvaluarVac003() throws Exception {
        long carla = datos.adjunto("Carla");
        datos.miembro(servicio, carla);
        long guardiaCarla = datos.guardia(carla, AGO_10, TipoGuardia.LOCALIZADA, false, servicio);
        CountDownLatch bloqueado = new CountDownLatch(1);
        ExecutorService hilo = Executors.newSingleThreadExecutor();
        try {
            // Otra transacción hace lo mismo que el registro de una cesión: bloquea el servicio y da a
            // Bruno la guardia del día 10. Se confirma mientras las vacaciones esperan el bloqueo.
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
            anadir(bruno, AGO_10)
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.infracciones[0].regla").value("VAC-003"));
            otra.get();
        } finally {
            hilo.shutdownNow();
        }
        assertThat(datos.contarVacaciones(bruno)).isZero();
        assertThat(datos.medicoDeGuardia(guardiaCarla)).isEqualTo(bruno);
    }

    @Test
    void vacacionesYCesionDelMismoDiaALaVez_soloUnaTieneExito() throws Exception {
        long carla = datos.adjunto("Carla");
        datos.miembro(servicio, carla);
        long guardiaCarla = datos.guardia(carla, AGO_10, TipoGuardia.LOCALIZADA, false, servicio);
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Integer>> tareas = List.of(
                    () -> {
                        salida.await();
                        return anadir(bruno, AGO_10).andReturn().getResponse().getStatus();
                    },
                    () -> {
                        salida.await();
                        return ceder(guardiaCarla, carla, bruno).andReturn().getResponse().getStatus();
                    });
            List<Future<Integer>> resultados = tareas.stream().map(hilos::submit).toList();
            salida.countDown();
            List<Integer> estados = new ArrayList<>();
            for (Future<Integer> r : resultados) {
                estados.add(r.get());
            }
            assertThat(Stream.of(estados.get(0), estados.get(1)).filter(e -> e == 200 || e == 201)).hasSize(1);
            assertThat(estados).contains(422);
        } finally {
            hilos.shutdownNow();
        }
        boolean vacaciones = datos.contarVacaciones(bruno) == 1;
        boolean guardia = datos.medicoDeGuardia(guardiaCarla) == bruno;
        assertThat(vacaciones ^ guardia).as("solo una de las dos operaciones queda aplicada").isTrue();
    }

    @Test
    void escenario11_tras_anadir_laCesionDeEseDiaSeRechazaPorCg012() throws Exception {
        long carla = datos.adjunto("Carla");
        datos.miembro(servicio, carla);
        long guardiaCarla = datos.guardia(carla, AGO_10, TipoGuardia.LOCALIZADA, false, servicio);

        anadir(bruno, AGO_10).andExpect(status().isOk());

        ceder(guardiaCarla, carla, bruno)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.infracciones[0].regla").value("CG-012"));
        assertThat(datos.medicoDeGuardia(guardiaCarla)).isEqualTo(carla);
    }
}
