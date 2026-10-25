package es.medoncalls.guardias.vacaciones.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Días que se añaden y que se quitan (contrato: `ModificarVacacionesRequest`). Listas, no
 * conjuntos: los repetidos se rechazan, no se descartan sin avisar.
 */
public record ModificarVacacionesRequestDto(List<LocalDate> anadir, List<LocalDate> quitar) {
}
