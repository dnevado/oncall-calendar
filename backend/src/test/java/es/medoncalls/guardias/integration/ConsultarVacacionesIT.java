package es.medoncalls.guardias.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;

/**
 * Consulta de las vacaciones del servicio (002 FR-001, FR-002, FR-012 a FR-014; research V5 y V7).
 * Hoy es {@link IntegracionBase#HOY} (2030-01-15).
 */
class ConsultarVacacionesIT extends IntegracionBase {

    private static final String RUTA = "/api/v1/servicios/{id}/vacaciones";

    private long servicio;
    private long otroServicio;
    private long admin;
    private long bruno;
    private long carla;
    private long dario;

    @BeforeEach
    void datosDelServicio() {
        servicio = datos.servicio("Cardiología");
        otroServicio = datos.servicio("Neumología");
        admin = datos.adjunto("Ana");
        bruno = datos.adjunto("Bruno");
        carla = datos.adjunto("Carla");
        dario = datos.adjunto("Darío");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, bruno);
        datos.miembro(servicio, carla, false, false);
        datos.miembro(otroServicio, dario);
    }

    @Test
    void devuelveTodosLosMedicosDelServicioConSusDiasOrdenados() throws Exception {
        datos.vacaciones(bruno, LocalDate.of(2030, 2, 11));
        datos.vacaciones(bruno, LocalDate.of(2030, 2, 10));
        datos.vacaciones(bruno, LocalDate.of(2030, 1, 5));
        datos.vacaciones(bruno, LocalDate.of(2030, 9, 1));
        datos.vacaciones(carla, LocalDate.of(2030, 3, 1));
        datos.vacaciones(dario, LocalDate.of(2030, 2, 10));

        mvc.perform(get(RUTA, servicio).session(login(admin)).param("desde", "2030-01-01").param("hasta", "2030-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].medico.id").value(admin))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].dias.length()").value(0))
                .andExpect(jsonPath("$[1].medico.id").value(bruno))
                .andExpect(jsonPath("$[1].medico.nombre").value("Bruno Prueba"))
                .andExpect(jsonPath("$[1].dias.length()").value(3))
                .andExpect(jsonPath("$[1].dias[0].fecha").value("2030-01-05"))
                .andExpect(jsonPath("$[1].dias[1].fecha").value("2030-02-10"))
                .andExpect(jsonPath("$[1].dias[2].fecha").value("2030-02-11"))
                .andExpect(jsonPath("$[1].dias[0].conflicto").doesNotExist())
                .andExpect(jsonPath("$[2].medico.id").value(carla))
                .andExpect(jsonPath("$[2].activo").value(false))
                .andExpect(jsonPath("$[2].dias[0].fecha").value("2030-03-01"));
    }

    @Test
    void filtraPorMedico() throws Exception {
        datos.vacaciones(bruno, LocalDate.of(2030, 2, 10));
        datos.vacaciones(carla, LocalDate.of(2030, 2, 10));

        mvc.perform(get(RUTA, servicio).session(login(admin)).param("desde", "2030-01-01")
                        .param("hasta", "2030-12-31").param("medicoId", String.valueOf(bruno)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].medico.id").value(bruno))
                .andExpect(jsonPath("$[0].dias.length()").value(1));
    }

    @Test
    void senalaComoConflictoSoloLaGuardiaDelMismoMedicoEnElServicio() throws Exception {
        datos.vacaciones(bruno, LocalDate.of(2030, 2, 10));
        datos.vacaciones(bruno, LocalDate.of(2030, 2, 11));
        datos.vacaciones(bruno, LocalDate.of(2030, 2, 12));
        long guardia = datos.guardia(bruno, LocalDate.of(2030, 2, 10), TipoGuardia.PRESENCIA, true, servicio);
        datos.guardia(bruno, LocalDate.of(2030, 2, 11), TipoGuardia.PRESENCIA, false, otroServicio);
        datos.guardia(admin, LocalDate.of(2030, 2, 12), TipoGuardia.PRESENCIA, false, servicio);

        mvc.perform(get(RUTA, servicio).session(login(admin)).param("desde", "2030-02-01")
                        .param("hasta", "2030-02-28").param("medicoId", String.valueOf(bruno)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dias[0].conflicto.id").value(guardia))
                .andExpect(jsonPath("$[0].dias[0].conflicto.tipo").value("PRESENCIA"))
                .andExpect(jsonPath("$[0].dias[0].conflicto.festivo").value(true))
                .andExpect(jsonPath("$[0].dias[0].conflicto.medico.id").value(bruno))
                .andExpect(jsonPath("$[0].dias[1].conflicto").doesNotExist())
                .andExpect(jsonPath("$[0].dias[2].conflicto").doesNotExist());
    }

    @Test
    void incluyeDiasPasados() throws Exception {
        datos.vacaciones(bruno, LocalDate.of(2029, 8, 1));

        mvc.perform(get(RUTA, servicio).session(login(admin)).param("desde", "2029-07-01")
                        .param("hasta", "2029-08-31").param("medicoId", String.valueOf(bruno)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dias[0].fecha").value("2029-08-01"));
    }

    @Test
    void rangoInvertidoOMayorDe731DiasEs400() throws Exception {
        MockHttpSession sesion = login(admin);
        mvc.perform(get(RUTA, servicio).session(sesion).param("desde", "2030-02-01").param("hasta", "2030-01-01"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(RUTA, servicio).session(sesion).param("desde", "2030-01-01").param("hasta", "2032-01-03"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(RUTA, servicio).session(sesion).param("desde", "2030-01-01").param("hasta", "2032-01-02"))
                .andExpect(status().isOk());
    }

    @Test
    void medicoDeOtroServicioEs404() throws Exception {
        mvc.perform(get(RUTA, servicio).session(login(admin)).param("desde", "2030-01-01")
                        .param("hasta", "2030-12-31").param("medicoId", String.valueOf(dario)))
                .andExpect(status().isNotFound());
    }

    @Test
    void unMedicoQueNoAdministraElServicioRecibe403() throws Exception {
        mvc.perform(get(RUTA, servicio).session(login(bruno)).param("desde", "2030-01-01").param("hasta", "2030-12-31"))
                .andExpect(status().isForbidden());
    }
}
