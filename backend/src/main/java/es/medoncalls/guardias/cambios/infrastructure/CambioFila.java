package es.medoncalls.guardias.cambios.infrastructure;

import java.time.LocalDateTime;

/** Fila de `guardias_cambios` con los nombres de cedente, receptor y administrador. */
public record CambioFila(
        Long id,
        String tipoCambio,
        String fechaIniCambio,
        String fechaFinCambio,
        Long cedenteId,
        String cedenteNombre,
        String cedenteApellidos,
        Long receptorId,
        String receptorNombre,
        String receptorApellidos,
        Long registradoPorId,
        String registradoPorNombre,
        String registradoPorApellidos,
        Long servicioId,
        String tipoGuardiaOrigen,
        Integer festivoOrigen,
        String tipoGuardiaDestino,
        Integer festivoDestino,
        LocalDateTime fechaHoraRegistro,
        String idempotencyKey) {
}
