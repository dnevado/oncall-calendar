package es.medoncalls.guardias.cambios.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import es.medoncalls.guardias.cambios.domain.ContextoValidacion.Participante;

/**
 * FR-012 / CG-013: quien recibe una guardia de presencia no puede tener presencia el día anterior
 * ni el posterior. Vale para cualquier médico que reciba una presencia, sin exención de residentes
 * simulados. No se mira el día adyacente que es la otra fecha del propio cambio (su guardia de ese
 * día cambia de manos).
 */
public final class ReglaPresenciasConsecutivas {

    /**
     * @param otraFechaDelCambio fecha de la otra guardia del cambio, o null en una CESION
     * @param infraccion crea la infracción CG-013 para el médico y el día adyacente
     */
    public List<Infraccion> evaluar(Participante quienRecibe, Guardia recibida, LocalDate otraFechaDelCambio,
            BiFunction<Participante, LocalDate, Infraccion> infraccion) {
        if (!recibida.esPresencia()) {
            return List.of();
        }
        List<Infraccion> infracciones = new ArrayList<>();
        for (LocalDate adyacente : List.of(recibida.fecha().minusDays(1), recibida.fecha().plusDays(1))) {
            if (adyacente.equals(otraFechaDelCambio)) {
                continue;
            }
            boolean presencia = quienRecibe.guardiasEn(adyacente).stream()
                    .anyMatch(g -> g.id() != recibida.id() && g.esPresencia());
            if (presencia) {
                infracciones.add(infraccion.apply(quienRecibe, adyacente));
            }
        }
        return infracciones;
    }
}
