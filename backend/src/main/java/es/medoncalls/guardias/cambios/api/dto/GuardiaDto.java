package es.medoncalls.guardias.cambios.api.dto;

import java.time.LocalDate;

import es.medoncalls.guardias.cambios.domain.TipoGuardia;
import es.medoncalls.guardias.cambios.infrastructure.GuardiaFila;

/** Guardia del calendario (contrato: `Guardia`). */
public record GuardiaDto(long id, LocalDate fecha, TipoGuardia tipo, boolean festivo, MedicoResumenDto medico) {

    public static GuardiaDto de(GuardiaFila fila) {
        var guardia = fila.aGuardia();
        return new GuardiaDto(guardia.id(), guardia.fecha(), guardia.tipo(), guardia.festivo(),
                new MedicoResumenDto(guardia.medicoId(), fila.nombreCompleto()));
    }
}
