package es.medoncalls.guardias.integration;

import static es.medoncalls.guardias.support.CsrfSpa.csrfSpa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import es.medoncalls.guardias.support.IntegracionBase;

/**
 * Toda operación bajo `/servicios/{servicioId}/**` exige ser administrador de ese servicio
 * (FR-001, CG-002; T062). Recorre los mapeos reales de los controladores, así que un endpoint nuevo
 * queda cubierto sin tocar la prueba: con una sesión de un médico que no administra el servicio,
 * todos responden 403.
 */
class ServicioAccessCoverageTest extends IntegracionBase {

    /** Parámetros válidos para cualquiera de las operaciones, para que no falle antes la validación. */
    private static final Map<String, String> PARAMETROS = Map.of(
            "desde", "2030-02-01", "hasta", "2030-02-28", "tipo", "CESION", "fechaDestino", "2030-02-15");
    private static final String CUERPO = "{\"tipo\":\"CESION\",\"guardiaOrigenId\":1,\"cedenteId\":1,\"receptorId\":2}";

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mapeos;

    @Test
    void todasLasOperacionesDelServicioDan403ANoAdministradores() throws Exception {
        long servicio = datos.servicio("Cardiología");
        long medico = datos.adjunto("Bruno");
        datos.miembro(servicio, medico);
        MockHttpSession sesion = login(medico);

        List<String> probadas = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> mapeo : mapeos.getHandlerMethods().entrySet()) {
            Set<String> rutas = mapeo.getKey().getPatternValues();
            for (String ruta : rutas) {
                if (!ruta.startsWith("/api/v1/servicios/")) {
                    continue;
                }
                for (RequestMethod metodo : mapeo.getKey().getMethodsCondition().getMethods()) {
                    String url = ruta.replaceAll("\\{servicioId}", String.valueOf(servicio)).replaceAll("\\{[^}]+}", "1");
                    var peticion = request(HttpMethod.valueOf(metodo.name()), url).session(sesion).with(csrfSpa());
                    PARAMETROS.forEach(peticion::param);
                    if (metodo == RequestMethod.POST) {
                        peticion.contentType(MediaType.APPLICATION_JSON).content(CUERPO);
                    }
                    int estado = mvc.perform(peticion).andReturn().getResponse().getStatus();
                    assertThat(estado).as("%s %s", metodo, ruta).isEqualTo(403);
                    probadas.add(metodo + " " + ruta);
                }
            }
        }
        assertThat(probadas).contains(
                "GET /api/v1/servicios/{servicioId}/guardias",
                "GET /api/v1/servicios/{servicioId}/guardias/{guardiaId}/receptores-candidatos",
                "GET /api/v1/servicios/{servicioId}/cambios",
                "POST /api/v1/servicios/{servicioId}/cambios");
    }
}
