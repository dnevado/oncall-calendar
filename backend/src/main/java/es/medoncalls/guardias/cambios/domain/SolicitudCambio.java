package es.medoncalls.guardias.cambios.domain;

/**
 * Petición de cambio tal como la registra el administrador. El cedente es el médico que se
 * espera que tenga la guardia origen (CG-005); en CAMBIO, guardiaDestinoId es la guardia del
 * receptor.
 */
public record SolicitudCambio(
        TipoCambio tipo,
        long servicioId,
        long guardiaOrigenId,
        long cedenteId,
        long receptorId,
        Long guardiaDestinoId) {
}
