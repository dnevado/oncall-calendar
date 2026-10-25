package es.medoncalls.guardias.vacaciones.api.dto;

import java.time.LocalDate;
import java.util.List;

import es.medoncalls.guardias.vacaciones.application.ResultadoModificacion;

/** Días que han cambiado (contrato: `ResultadoVacaciones`). */
public record ResultadoVacacionesDto(List<LocalDate> anadidos, List<LocalDate> quitados) {

    public static ResultadoVacacionesDto de(ResultadoModificacion resultado) {
        return new ResultadoVacacionesDto(resultado.anadidos(), resultado.quitados());
    }
}
