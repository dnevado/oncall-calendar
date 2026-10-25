package es.medoncalls.guardias.vacaciones.api.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import es.medoncalls.guardias.cambios.api.dto.GuardiaDto;

/** Día de vacaciones con su conflicto, si lo hay (contrato: `DiaVacaciones`). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DiaVacacionesDto(LocalDate fecha, GuardiaDto conflicto) {
}
