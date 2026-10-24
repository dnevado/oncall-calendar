package es.medoncalls.guardias.contract;

import static es.medoncalls.guardias.contract.OpenApiContract.cumple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.support.IntegracionBase;

class GuardiasContractTest extends IntegracionBase {

    @Test
    void listaLasGuardiasDelRangoSegunElContrato() throws Exception {
        long servicio = datos.servicio("Cardiología");
        long admin = datos.adjunto("Ana");
        long residente = datos.residente("Rita", "R2");
        datos.miembro(servicio, admin, true, true);
        datos.miembro(servicio, residente);
        long presencia = datos.guardia(admin, LocalDate.of(2030, 1, 20), TipoGuardia.PRESENCIA, true, servicio);
        datos.guardia(residente, LocalDate.of(2030, 1, 20), TipoGuardia.RESIDENTE, false, servicio);
        datos.guardia(admin, LocalDate.of(2030, 2, 20), TipoGuardia.LOCALIZADA, false, servicio);

        mvc.perform(get("/api/v1/servicios/{id}/guardias", servicio).session(login(admin))
                        .param("desde", "2030-01-01").param("hasta", "2030-01-31"))
                .andExpect(status().isOk())
                .andExpect(cumple())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(presencia))
                .andExpect(jsonPath("$[0].fecha").value("2030-01-20"))
                .andExpect(jsonPath("$[0].tipo").value("PRESENCIA"))
                .andExpect(jsonPath("$[0].festivo").value(true))
                .andExpect(jsonPath("$[0].medico.nombre").value("Ana Prueba"))
                .andExpect(jsonPath("$[1].tipo").value("RESIDENTE"));
    }

    @Test
    void rangoInvertidoEs400() throws Exception {
        long servicio = datos.servicio("Cardiología");
        long admin = datos.adjunto("Ana");
        datos.miembro(servicio, admin, true, true);

        mvc.perform(get("/api/v1/servicios/{id}/guardias", servicio).session(login(admin))
                        .param("desde", "2030-01-31").param("hasta", "2030-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(cumple());
    }
}
