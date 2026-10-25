package es.medoncalls.guardias.vacaciones.api.dto;

import java.util.List;

import es.medoncalls.guardias.cambios.api.dto.GuardiaDto;
import es.medoncalls.guardias.cambios.api.dto.MedicoResumenDto;
import es.medoncalls.guardias.cambios.infrastructure.FechaLegacy;
import es.medoncalls.guardias.vacaciones.application.VacacionesDelMedico;

/** Médico del servicio con sus días de vacaciones (contrato: `VacacionesMedico`). */
public record VacacionesMedicoDto(MedicoResumenDto medico, boolean activo, List<DiaVacacionesDto> dias) {

    public static VacacionesMedicoDto de(VacacionesDelMedico vacaciones) {
        var fila = vacaciones.medico();
        MedicoResumenDto medico = new MedicoResumenDto(fila.id(), fila.nombreCompleto());
        List<DiaVacacionesDto> dias = vacaciones.dias().stream()
                .map(d -> new DiaVacacionesDto(FechaLegacy.fecha(d.fecha()), d.conflicto()
                        .map(g -> new GuardiaDto(g.id(), g.fecha(), g.tipo(), g.festivo(), medico))
                        .orElse(null)))
                .toList();
        return new VacacionesMedicoDto(medico, fila.activo(), dias);
    }
}
