package es.medoncalls.guardias.contract;

import static es.medoncalls.guardias.contract.OpenApiContract.cumple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;

/** Las operaciones de vacaciones cumplen `contracts/guardias-cambios.openapi.yaml` 1.2.0 (principio IV). */
class VacacionesContractTest extends IntegracionBase {

    private long servicio;
    private long admin;
    private long bruno;
    private long ajeno;

    @BeforeEach
    void datosDelServicio() {
        servicio = datos.servicio("Cardiología");
        admin = datos.adjunto("Ana");
        bruno = datos.adjunto("Bruno");
        ajeno = datos.adjunto("Darío");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, bruno, false, false);
        datos.miembro(datos.servicio("Neumología"), ajeno);
    }

    @Test
    void listarVacacionesCumpleElContrato() throws Exception {
        datos.vacaciones(bruno, LocalDate.of(2030, 2, 10));
        datos.vacaciones(bruno, LocalDate.of(2030, 2, 11));
        datos.guardia(bruno, LocalDate.of(2030, 2, 10), TipoGuardia.LOCALIZADA, false, servicio);

        mvc.perform(get("/api/v1/servicios/{id}/vacaciones", servicio).session(login(admin))
                        .param("desde", "2030-01-01").param("hasta", "2030-06-30"))
                .andExpect(status().isOk())
                .andExpect(cumple());
    }

    @Test
    void erroresDeListarVacacionesCumplenElContrato() throws Exception {
        mvc.perform(get("/api/v1/servicios/{id}/vacaciones", servicio).session(login(admin))
                        .param("desde", "2030-02-01").param("hasta", "2030-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(cumple());
        mvc.perform(get("/api/v1/servicios/{id}/vacaciones", servicio).session(login(admin))
                        .param("desde", "2030-01-01").param("hasta", "2030-01-31")
                        .param("medicoId", String.valueOf(ajeno)))
                .andExpect(status().isNotFound())
                .andExpect(cumple());
        mvc.perform(get("/api/v1/servicios/{id}/vacaciones", servicio).session(login(bruno))
                        .param("desde", "2030-01-01").param("hasta", "2030-01-31"))
                .andExpect(status().isForbidden())
                .andExpect(cumple());
    }
}
