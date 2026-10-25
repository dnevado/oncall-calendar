package es.medoncalls.guardias.vacaciones.infrastructure;

import java.util.Optional;

import es.medoncalls.guardias.cambios.domain.Guardia;
import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.cambios.infrastructure.FechaLegacy;

/**
 * Día de vacaciones con la guardia del mismo médico ese día en el servicio consultado, si la hay
 * (conflicto, research V5). Las columnas de la guardia son null cuando no hay conflicto.
 */
public record DiaVacacionFila(
        Long medicoId,
        String fecha,
        Long guardiaId,
        Integer guardiaFestivo,
        String guardiaTipo,
        Long guardiaServicioId) {

    public Optional<Guardia> conflicto() {
        if (guardiaId == null) {
            return Optional.empty();
        }
        return Optional.of(new Guardia(guardiaId, medicoId, FechaLegacy.fecha(fecha), TipoGuardia.desdeLegacy(guardiaTipo),
                guardiaFestivo != null && guardiaFestivo != 0, guardiaServicioId));
    }
}
