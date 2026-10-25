package es.medoncalls.guardias.vacaciones.application;

import java.time.LocalDate;
import java.util.List;

/** Días que realmente han cambiado (contrato: `ResultadoVacaciones`). */
public record ResultadoModificacion(List<LocalDate> anadidos, List<LocalDate> quitados) {
}
